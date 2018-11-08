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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.api.eaas.ComponentGroup;
import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.emucomp.api.NodeTcpConfiguration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.client.ComponentGroupClient;
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
    private ComponentGroupClient groupClient;

    @Inject
    @Config(value = "ws.eaasgw")
    private String eaasGw;
    
    @POST
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
            // create new empty group
            String groupId = groupClient.getComponentGroupPort(eaasGw).createGroup();
            networkResponse = new NetworkResponse(groupId);

            // a switch comes included with every network group
            NetworkSwitchConfiguration switchConfig = new NetworkSwitchConfiguration();
            String switchId = eaasClient.getEaasWSPort(eaasGw).createSession(switchConfig.value(false));
    
            groupClient.getComponentGroupPort(eaasGw).add(groupId, switchId);

            String nodeTcpId = null;
            if (network.hasInternet()) {
                VdeSlirpConfiguration slirpConfig = new VdeSlirpConfiguration();
                String slirpMac = slirpConfig.getHwAddress();
                String slirpId = eaasClient.getEaasWSPort(eaasGw).createSession(slirpConfig.value(false));
                groupClient.getComponentGroupPort(eaasGw).add(groupId, slirpId);

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
                groupClient.getComponentGroupPort(eaasGw).add(groupId, nodeTcpId);

                Map<String, URI> controlUrls = ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw).getControlUrls(nodeTcpId));
                String nodeTcpUrl = controlUrls.get("ws+ethernet+" + nodeConfig.getHwAddress()).toString();
                componentClient.getNetworkSwitchPort(eaasGw).connect(switchId, nodeTcpUrl);

                String nodeInfoUrl = controlUrls.get("info").toString();
                System.out.println(nodeInfoUrl);
                networkResponse.addUrl("tcp", URI.create(nodeInfoUrl));
            }

            // add all the other components
            for (NetworkRequest.ComponentSpec component : network.getComponents()) {
                this.addComponent(groupId, switchId, component);
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/components")
    public void addComponent(@PathParam("groupId") String groupId, NetworkRequest.ComponentSpec component, @Context final HttpServletResponse response) {
        try {
            final String switchId = this.findSwitchId(groupId);
            this.addComponent(groupId, switchId, component);
        }
        catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not add component to group!", error.getMessage()))
                    .build());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }

    @DELETE
    @Path("/{groupId}/components/{componentId}")
    public void removeComponent(@PathParam("groupId") String groupId, @PathParam("componentId") String componentId, @Context final HttpServletResponse response) {
        try {
            final String switchId = this.findSwitchId(groupId);
            this.removeComponent(groupId, switchId, componentId);
        }
        catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not remove component from group!", error.getMessage()))
                    .build());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }

    @GET
    @Path("/{groupId}/wsConnection")
    public String wsConnection(@PathParam("groupId") String groupId)
    {
        try {
            String switchId = findSwitchId(groupId);
            return componentClient.getNetworkSwitchPort(eaasGw).wsConnect(switchId);
        } catch (BWFLAException e) {
            e.printStackTrace();
            throw new ServerErrorException(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation(
                            "Could not find switch for group: " + groupId , e.getMessage()))
                    .build());
        }
    }

    @GET
    @Path("/{groupId}")
    public Collection<GroupComponent> listComponents(@PathParam("groupId") String groupId) {
        try {
            Collection<GroupComponent> result = new HashSet<GroupComponent>();
            
            List<String> components = groupClient.getComponentGroupPort(eaasGw).list(groupId);
            for (String componentId : components) {
                String type = componentClient.getComponentPort(eaasGw).getComponentType(componentId);
                
                try {
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
    
    @POST
    @Path("/{groupId}/keepalive")
    public void keepalive(@PathParam("groupId") String groupId) {
        try {
            groupClient.getComponentGroupPort(eaasGw).keepalive(groupId);
            return;
        } catch (BWFLAException e) {
            throw new NotFoundException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInformation(
                            "Could not send keepalive request.", e.getMessage()))
                    .build());
        }
    }

    private void addComponent(String groupId, String switchId, NetworkRequest.ComponentSpec component) {
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
            groupClient.getComponentGroupPort(eaasGw).add(groupId, component.getComponentId());

        } catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not acquire group information.", error.getMessage()))
                    .build());
        }
    }

    private void removeComponent(String groupId, String switchId, String componentId) {
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
            groupClient.getComponentGroupPort(eaasGw).remove(groupId, componentId);
        }
        catch (BWFLAException error) {
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorInformation("Could not acquire group information.", error.getMessage()))
                    .build());
        }
    }

    private String findSwitchId(String groupId) throws BWFLAException {
        final ComponentGroup group = groupClient.getComponentGroupPort(eaasGw);
        final Component component = componentClient.getComponentPort(eaasGw);
        for (String cid : group.list(groupId)) {
            final String type = component.getComponentType(cid);
            if (type.contentEquals("switch"))
                return cid;
        }

        throw new BWFLAException("No network-switch found in component group '" + groupId + "'!");
    }

    private Map<String, URI> getControlUrls(String componentId) throws BWFLAException {
        return ComponentClient.controlUrlsToMap(componentClient.getComponentPort(eaasGw)
                .getControlUrls(componentId));
    }
}
