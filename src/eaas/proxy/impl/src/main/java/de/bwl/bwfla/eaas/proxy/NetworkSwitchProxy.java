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

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.api.emucomp.NetworkSwitch;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.SessionRegistry;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;

import java.net.URI;
import java.net.URISyntaxException;


@MTOM
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebServlet("/ComponentProxy/NetworkSwitch")
@WebService(targetNamespace = "http://bwfla.bwl.de/api/emucomp", serviceName = "NetworkSwitchService", portName = "NetworkSwitchPort")
public class NetworkSwitchProxy implements NetworkSwitch
{
    @Inject
    DirectComponentClient componentClient;

    @Inject
    private SessionRegistry sessions = null;

    protected NetworkSwitch getNetworkSwitch(String componentId) throws BWFLAException {
        final SessionRegistry.Entry session = sessions.lookup(componentId);
        if(session == null)
            throw new BWFLAException("Network session for switch: " + componentId + " not found");
        final ResourceHandle resource = session.getResourceHandle();
        return componentClient.getNetworkSwitchPort(resource.getNodeID());
    }

    @Override
    public void connect(String componentId, String url) throws BWFLAException {
        getNetworkSwitch(componentId).connect(componentId, url);
    }

    @Override
    public void disconnect(String componentId, String url) throws BWFLAException {
        getNetworkSwitch(componentId).disconnect(componentId, url);
    }

    @Override
    public String wsConnect(String componentId) throws BWFLAException {

        final SessionRegistry.Entry session = sessions.lookup(componentId);
        String componentHost = session.getResourceHandle().getNodeID().getNodeAddress();
        // FIXME make protocol check more generic
        if(!componentHost.contains("http://") && !componentHost.contains("https://")){
            componentHost = "http://" + componentHost;
        }
        final URI hostURI = URI.create(componentHost);

        String wsAddress = getNetworkSwitch(componentId).wsConnect(componentId);

        URI orig = URI.create(wsAddress);
        try {
            URI uri = new URI(orig.getScheme(),
                    orig.getUserInfo(),
                    hostURI.getHost(),
                    hostURI.getPort(),
                    orig.getPath(),
                    orig.getQuery(),
                    orig.getFragment());
            return uri.normalize().toString();
        } catch (URISyntaxException ex) {
            // this catch clause mimicks the behaviour of URI.create()
            throw new BWFLAException(ex.getMessage(), ex);
        }
    }
}