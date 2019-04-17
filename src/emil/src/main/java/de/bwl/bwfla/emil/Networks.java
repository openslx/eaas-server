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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.api.emucomp.GetControlUrlsResponse;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import de.bwl.bwfla.emil.session.NetworkSession;
import de.bwl.bwfla.emil.session.Session;
import de.bwl.bwfla.emil.session.SessionManager;
import de.bwl.bwfla.emucomp.api.NodeTcpConfiguration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.client.EaasClient;
import de.bwl.bwfla.emil.datatypes.ErrorInformation;
import de.bwl.bwfla.emil.datatypes.GroupComponent;
import de.bwl.bwfla.emil.datatypes.NetworkRequest;
import de.bwl.bwfla.emil.datatypes.NetworkResponse;
import de.bwl.bwfla.emucomp.api.NetworkSwitchConfiguration;
import de.bwl.bwfla.emucomp.api.VdeSlirpConfiguration;
import de.bwl.bwfla.emucomp.client.ComponentClient;

@Path("/networks")
@ApplicationScoped
public class Networks {
    @Inject
    private EaasClient eaasClient;
    
    @Inject
    private ComponentClient componentClient;

    @Inject
    private SessionManager sessions = null;

    @Inject
    @Config(value = "ws.eaasgw")
    private String eaasGw;
    
    @POST
    @Secured
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public NetworkResponse createNetwork(NetworkRequest network, @Context final HttpServletResponse response) {
        if (network.getComponents() == null) {
            throw new BadRequestException(
                    Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation("No components field given in the input data."))
                    .build());
        }

        NetworkResponse networkResponse = null;
        try {
            // a switch comes included with every network group
            NetworkSwitchConfiguration switchConfig = new NetworkSwitchConfiguration();
            String switchId = eaasClient.getEaasWSPort(eaasGw).createSession(switchConfig.value(false));

            Session session = sessions.createNetworkSession(switchId);
            networkResponse = new NetworkResponse(session.id());

            String nodeTcpId = null;
            if (network.hasInternet()) {
                VdeSlirpConfiguration slirpConfig = new VdeSlirpConfiguration();
                String slirpMac = slirpConfig.getHwAddress();
                String slirpId = eaasClient.getEaasWSPort(eaasGw).createSession(slirpConfig.value(false));
                sessions.addComponent(session, slirpId);

                Map<String, URI> controlUrls = ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw).getControlUrls(slirpId));
                String slirpUrl = controlUrls.get("ws+ethernet+" + slirpMac).toString();

                componentClient.getNetworkSwitchPort(eaasGw).connect(switchId, slirpUrl);
            }

            if(network.isTcpGateway() && network.getTcpGatewayConfig() != null) {
                NetworkRequest.TcpGatewayConfig tcpGatewayConfig = network.getTcpGatewayConfig();

                NodeTcpConfiguration nodeConfig = new NodeTcpConfiguration();
                nodeConfig.setHwAddress(NetworkUtils.getRandomHWAddress());

                nodeConfig.setPrivateNetIp(tcpGatewayConfig.getGwPrivateIp());
                nodeConfig.setPrivateNetMask(tcpGatewayConfig.getGwPrivateMask());

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

                nodeTcpId = eaasClient.getEaasWSPort(eaasGw).createSession(nodeConfig.value(false));
                sessions.addComponent(session, nodeTcpId);

                Map<String, URI> controlUrls = ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw).getControlUrls(nodeTcpId));
                String nodeTcpUrl = controlUrls.get("ws+ethernet+" + nodeConfig.getHwAddress()).toString();
                componentClient.getNetworkSwitchPort(eaasGw).connect(switchId, nodeTcpUrl);

                String nodeInfoUrl = controlUrls.get("info").toString();
                System.out.println(nodeInfoUrl);
                networkResponse.addUrl("tcp", URI.create(nodeInfoUrl));
            }

            // add all the other components
            for (NetworkRequest.ComponentSpec component : network.getComponents()) {
                this.addComponent(session, switchId, component);
            }
            
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return networkResponse;
        } catch (BWFLAException | JAXBException e) {
            throw new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Server has encountered an internal error.", e.getMessage()))
                    .build(), e);
        }
    }

    @POST
    @Secured
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
    @Secured
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
    @Secured
    @Path("/{id}/components/{componentId}")
    public void removeComponent(@PathParam("id") String id, @PathParam("componentId") String componentId, @Context final HttpServletResponse response) {
        try {
            Session session = sessions.get(id);
            if(session == null || !(session instanceof NetworkSession))
                throw new BWFLAException("session not found " + id);

            final String switchId = ((NetworkSession) session).getSwitchId();
            this.removeComponent(session, switchId, componentId);
        }
        catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not remove component from group!", error.getMessage()))
                    .build());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }

    @GET
    @Secured
    @Path("/{id}/wsConnection")
    public String wsConnection(@PathParam("id") String id)
    {
        try {
            Session session = sessions.get(id);
            if(session == null || !(session instanceof NetworkSession))
                throw new BWFLAException("session not found " + id);

            final String switchId = ((NetworkSession) session).getSwitchId();
            return componentClient.getNetworkSwitchPort(eaasGw).wsConnect(switchId);
        } catch (BWFLAException e) {
            e.printStackTrace();
            throw new ServerErrorException(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation(
                            "Could not find switch for session: " + id , e.getMessage()))
                    .build());
        }
    }

    @GET
    @Secured
    @Path("/{id}")
    public Collection<GroupComponent> listComponents(@PathParam("id") String id) {
        try {
            Collection<GroupComponent> result = new HashSet<GroupComponent>();

            Session session = sessions.get(id);
            if(session == null || !(session instanceof NetworkSession))
                throw new BWFLAException("session not found " + id);

            List<String> components = sessions.getComponents(session);
            for (String componentId : components) {
                String type = componentClient.getComponentPort(eaasGw).getComponentType(componentId);


                
                try {
                    if(type.equals("nodetcp")) {
                        System.out.println("!!!!!!!! nodetcp");
                        NetworkResponse networkResponse = new NetworkResponse(session.id());

                        Map<String, URI> controlUrls = ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw).getControlUrls(componentId));

                        String nodeInfoUrl = controlUrls.get("info").toString();
                        result.add(new GroupComponent(componentId, type, new URI("../components/" + componentId),
                                networkResponse ));
                        networkResponse.addUrl("tcp", URI.create(nodeInfoUrl));
                        result.add(new GroupComponent(componentId, type, new URI("../components/" + componentId),
                                networkResponse ));

                    } else
                        result.add(new GroupComponent(componentId, type, new URI("../components/" + componentId)));
                } catch (URISyntaxException e) {
                    throw new ServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new ErrorInformation(
                                    "An internal server error occurred.", e.getMessage()))
                            .build(), e);
                } 
            }
            return result;
        } catch (BWFLAException e) {
            throw new ServerErrorException(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation(
                            "Could not acquire group information.", e.getMessage()))
                    .build());
        }
    }

    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Session> getAllGroupIds() {
            return sessions.list();
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

            if (addToGroup)
                sessions.addComponent(session, component.getComponentId());

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
            sessions.removeComponent(session, componentId);
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
