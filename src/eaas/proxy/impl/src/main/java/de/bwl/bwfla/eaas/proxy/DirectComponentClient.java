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

package de.bwl.bwfla.eaas.proxy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;

import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.api.emucomp.Container;
import de.bwl.bwfla.api.emucomp.Machine;
import de.bwl.bwfla.api.emucomp.NetworkSwitch;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.emucomp.client.ComponentClient;

@ApplicationScoped
@Typed(DirectComponentClient.class)
public class DirectComponentClient extends ComponentClient {
    // TODO: should only be "/ComponentService?wsdl", because this is the public
    //       interface of a web service
    final private String WSDL_URL_TEMPLATE = "%s/emucomp/ComponentService?wsdl";
    
    @Override
    protected String getWsdlUrl(String host) {
        return String.format(WSDL_URL_TEMPLATE, host);
    }
    
    public <P> P getPort(NodeID nodeId, Class<P> port) throws BWFLAException {
        return getPort(nodeId.getNodeHost(), port);
    }
    
    public Component getComponentPort(NodeID nodeId) throws BWFLAException {
        return getPort(nodeId, Component.class);
    }

    public Container getContainerPort(NodeID nodeId) throws BWFLAException {
        return getPort(nodeId, Container.class);
    }

    public Machine getMachinePort(NodeID nodeId) throws BWFLAException {
        return getPort(nodeId, Machine.class);
    }
    
    public NetworkSwitch getNetworkSwitchPort(NodeID nodeId) throws BWFLAException {
        return getPort(nodeId, NetworkSwitch.class);
    }
}
