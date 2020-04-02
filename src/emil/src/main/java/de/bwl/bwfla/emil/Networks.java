/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.emil;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.utils.JsonBuilder;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.emil.datatypes.rest.NodeTcpComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.SlirpComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.SwitchComponentRequest;
import de.bwl.bwfla.emil.session.NetworkSession;
import de.bwl.bwfla.emil.session.Session;
import de.bwl.bwfla.emil.session.SessionComponent;
import de.bwl.bwfla.emil.session.SessionManager;
import de.bwl.bwfla.emucomp.api.NodeTcpConfiguration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.rest.ErrorInformation;
import de.bwl.bwfla.emil.datatypes.NetworkRequest;
import de.bwl.bwfla.emil.datatypes.NetworkResponse;
import de.bwl.bwfla.emucomp.api.NetworkSwitchConfiguration;
import de.bwl.bwfla.emucomp.api.VdeSlirpConfiguration;
import de.bwl.bwfla.emucomp.client.ComponentClient;

import static de.bwl.bwfla.emil.EmilRest.DEFAULT_RESPONSE_CAPACITY;

@Path("/networks")
@ApplicationScoped
public class Networks {
    @Inject
    private ComponentClient componentClient;

    @Inject
    private SessionManager sessions = null;

    @Inject
    private Components components = null;

    @Inject
    @Config(value = "ws.eaasgw")
    private String eaasGw;

    protected final static Logger LOG = Logger.getLogger(Networks.class.getName());

    @POST
    @Secured(roles = {Role.PUBLIC})
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public NetworkResponse createNetwork(NetworkRequest networkRequest, @Context final HttpServletResponse response) {
        if (networkRequest.getComponents() == null) {
            throw new BadRequestException(
                    Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation("No components field given in the input data."))
                    .build());
        }

        NetworkResponse networkResponse = null;
        try {
            // a switch comes included with every network group
            final SwitchComponentRequest switchComponentRequest = new SwitchComponentRequest();
            switchComponentRequest.setConfig(new NetworkSwitchConfiguration());
            final String switchId = components.createComponent(switchComponentRequest).getId();
            final NetworkSession session = new NetworkSession(switchId, networkRequest);
            session.components()
                    .add(new SessionComponent(switchId));

            sessions.register(session);

            networkResponse = new NetworkResponse(session.id());

            if (networkRequest.hasInternet()) {
                final String slirpMac = new VdeSlirpConfiguration().getHwAddress();
                SlirpComponentRequest slirpConfig = new SlirpComponentRequest();
                slirpConfig.setHwAddress(slirpMac);
                slirpConfig.setDhcp(false);

                if (networkRequest.getGateway() != null){
                    slirpConfig.setDhcp(true);
                    slirpConfig.setGateway(networkRequest.getGateway());
                }
                if (networkRequest.getNetwork() != null)
                    slirpConfig.setIp4Address(networkRequest.getNetwork());

                final String slirpId = components.createComponent(slirpConfig).getId();
                session.components()
                        .add(new SessionComponent(slirpId));

                Map<String, URI> controlUrls = ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw).getControlUrls(slirpId));
                String slirpUrl = controlUrls.get("ws+ethernet+" + slirpMac).toString();

                componentClient.getNetworkSwitchPort(eaasGw).connect(switchId, slirpUrl);
            }


//            if(network.isDhcp())
//            {
//                NodeTcpConfiguration nodeConfig = new NodeTcpConfiguration();
//                nodeConfig.setDhcp(true);
//                nodeConfig.setDhcpNetworkAddress(network.getDhcpNetworkAddress());
//                nodeConfig.setDhcpNetworkMask(network.getDhcpNetworkMask());
//                nodeConfig.setHwAddress(NetworkUtils.getRandomHWAddress());
//
//                String dhcpId = eaasClient.getEaasWSPort(eaasGw).createSession(nodeConfig.value(false));
//                sessions.addComponent(session, dhcpId);
//
//                Map<String, URI> controlUrls = ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw).getControlUrls(dhcpId));
//                String dhcpUrl = controlUrls.get("ws+ethernet+" + nodeConfig.getHwAddress()).toString();
//                componentClient.getNetworkSwitchPort(eaasGw).connect(switchId, dhcpUrl);
//            }

            if(networkRequest.isTcpGateway() && networkRequest.getTcpGatewayConfig() != null) {
                String nodeTcpId = null;
                NetworkRequest.TcpGatewayConfig tcpGatewayConfig = networkRequest.getTcpGatewayConfig();

                NodeTcpConfiguration nodeConfig = new NodeTcpConfiguration();
                nodeConfig.setHwAddress(NetworkUtils.getRandomHWAddress());

                if(tcpGatewayConfig.isSocks())
                {
                    nodeConfig.setSocksMode(true);
                    //      nodeConfig.setSocksUser("eaas");
                    //      nodeConfig.setSocksPasswd("bwfla");
                }
                else {
                    if(tcpGatewayConfig.getServerIp() == null || tcpGatewayConfig.getServerPort() == null)
                        throw new BWFLAException("invalid server/gateway config");

                    nodeConfig.setDestIp(tcpGatewayConfig.getServerIp());
                    nodeConfig.setDestPort(tcpGatewayConfig.getServerPort());
                }

                final NodeTcpComponentRequest nodeComponentRequest = new NodeTcpComponentRequest();
                nodeComponentRequest.setConfig(nodeConfig);
                nodeTcpId = components.createComponent(nodeComponentRequest).getId();
                session.components()
                        .add(new SessionComponent(nodeTcpId));

                Map<String, URI> controlUrls = ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw).getControlUrls(nodeTcpId));
                String nodeTcpUrl = controlUrls.get("ws+ethernet+" + nodeConfig.getHwAddress()).toString();
                componentClient.getNetworkSwitchPort(eaasGw).connect(switchId, nodeTcpUrl);

                String nodeInfoUrl = controlUrls.get("info").toString();
                System.out.println(nodeInfoUrl);
                networkResponse.addUrl("tcp", URI.create(nodeInfoUrl));
            }

            // add all the other components
            for (NetworkRequest.ComponentSpec component : networkRequest.getComponents()) {
                this.addComponent(session, switchId, component);
            }
            
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return networkResponse;
        }
        catch (Exception error) {
            throw Components.newInternalError(error);
        }
    }

    @POST
    @Secured(roles = {Role.PUBLIC})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/components")
    public void addComponent(@PathParam("id") String id, NetworkRequest.ComponentSpec component, @Context final HttpServletResponse response) {
        try {
            Session session = sessions.get(id);
            if(session == null || !(session instanceof NetworkSession))
                throw new BWFLAException("session not found " + id);

            final String switchId = ((NetworkSession) session).getSwitchId();
            this.addComponent(session, switchId, component);
        }
        catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not add component to group!", error.getMessage()))
                    .build());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }

    @POST
    @Secured(roles = {Role.PUBLIC})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/addComponentToSwitch")
    public void addComponentToSwitch(@PathParam("id") String id, NetworkRequest.ComponentSpec component, @Context final HttpServletResponse response) {
        try {
            Session session = sessions.get(id);
            if(session == null || !(session instanceof NetworkSession))
                throw new BWFLAException("session not found " + id);

            final String switchId = ((NetworkSession) session).getSwitchId();
            this.addComponent(session, switchId, component, false);
        }
        catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not add component to group!", error.getMessage()))
                    .build());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }

    @DELETE
    @Secured(roles = {Role.RESTRCITED})
    @Path("/{id}/components/{componentId}")
    public void removeComponent(@PathParam("id") String id, @PathParam("componentId") String componentId, @Context final HttpServletResponse response) {
 //       try {
            Session session = sessions.get(id);
            if(session == null || !(session instanceof NetworkSession))
            {
                LOG.severe("removeComponent: session not found " + id);
                return;
            }

            final String switchId = ((NetworkSession) session).getSwitchId();
            this.removeComponent(session, switchId, componentId);
 //       }
//        catch (BWFLAException error) {
//            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity(new ErrorInformation("Could not remove component from group!", error.getMessage()))
//                    .build());
//        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }

    @GET
    @Secured(roles = {Role.PUBLIC})
    @Path("/{id}/wsConnection")
    @Produces(MediaType.APPLICATION_JSON)
    public Response wsConnection(@PathParam("id") String id)
    {
        try {
            Session session = sessions.get(id);
            if(session == null || !(session instanceof NetworkSession))
                throw new BWFLAException("session not found " + id);

            final String switchId = ((NetworkSession) session).getSwitchId();
            String link = componentClient.getNetworkSwitchPort(eaasGw).wsConnect(switchId);
            JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
            json.beginObject();
            json.add("wsConnection", link);
            json.add("ok", true);
            json.endObject();
            json.finish();
            return Emil.createResponse(Response.Status.OK, json.toString());
        } catch (IOException | BWFLAException e) {
            e.printStackTrace();
            throw new ServerErrorException(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation(
                            "Could not find switch for session: " + id , e.getMessage()))
                    .build());
        }
    }

    private void addComponent(Session session, String switchId, NetworkRequest.ComponentSpec component) {
        addComponent(session, switchId, component, true);
    }

    private void addComponent(Session session, String switchId, NetworkRequest.ComponentSpec component, boolean addToGroup) {
        try {

            final Map<String, URI> map = this.getControlUrls(component.getComponentId());

            URI uri;
            if (component.getHwAddress().equals("auto")) {
                uri = map.entrySet().stream()
                        .filter(e -> e.getKey().startsWith("ws+ethernet+"))
                        .findAny()
                        .orElseThrow(() -> new InternalServerErrorException(
                                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new ErrorInformation(
                                                "Server has encountered an internal error.",
                                                "Cannot find suitable ethernet URI for requested component."))
                                        .build()))
                        .getValue();
            } else {
                uri = map.get("ws+ethernet+" + component.getHwAddress());
            }
            componentClient.getNetworkSwitchPort(eaasGw).connect(switchId, uri.toString());

            if (addToGroup) {
                session.components()
                        .add(new SessionComponent(component.getComponentId()));
            }

        } catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not acquire group information.", error.getMessage()))
                    .build());
        }
    }

    private void removeComponent(Session session, String switchId, String componentId) {
        try {
            final Map<String, URI> map = this.getControlUrls(componentId);
            final String ethurl = map.entrySet().stream()
                    .filter(e -> e.getKey().startsWith("ws+ethernet+"))
                    .findAny()
                    .orElseThrow(() -> new InternalServerErrorException(
                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                    .entity(new ErrorInformation("Server has encountered an internal error.",
                                                "Cannot find suitable ethernet URI for requested component."))
                                    .build()))
                    .getValue().toString();

            componentClient.getNetworkSwitchPort(eaasGw).disconnect(switchId, ethurl);
            sessions.remove(session.id(), componentId);
        }
        catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not acquire group information.", error.getMessage()))
                    .build());
        }
    }

    private Map<String, URI> getControlUrls(String componentId) throws BWFLAException {
        return ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw)
                .getControlUrls(componentId));
    }
}
