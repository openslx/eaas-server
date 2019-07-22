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

import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.api.emucomp.GetControlUrlsResponse.Return;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.SessionRegistry;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;

@MTOM
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebServlet("/ComponentProxy/Component")
@WebService(targetNamespace = "http://bwfla.bwl.de/api/emucomp", serviceName = "ComponentService", portName = "ComponentPort")
public class ComponentProxy implements Component {
    @Inject
    DirectComponentClient client;

    @Inject
    private SessionRegistry sessions = null;

    protected Component getComponent(String componentId) throws BWFLAException {
        try {
            final SessionRegistry.Entry session = sessions.lookup(componentId);
            if(session == null)
                throw new BWFLAException("session not found for component " + componentId);
            final ResourceHandle resource = session.getResourceHandle();
            return client.getComponentPort(resource.getNodeID());
        } catch (BWFLAException e) {
            throw new BWFLAException("Failure to access node's webservice: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String initialize(String arg0, String arg1) throws BWFLAException {
        throw new UnsupportedOperationException("Direct initialization of components is not allowed. Use the public EaaS API instead");
    }

    @Override
    public void destroy(String arg0) {
        throw new UnsupportedOperationException("Direct destruction of components is not allowed. Use the public EaaS API instead");
    }

    @Override
    public void keepalive(String componentId) throws BWFLAException {
        Component c = getComponent(componentId);
        if( c == null )
            throw new BWFLAException("component not found: " + componentId);
        c.keepalive(componentId);
    }

    @Override
    public String getState(String componentId) throws BWFLAException {
        return getComponent(componentId).getState(componentId);
    }

    @Override
    public String getComponentType(String componentId) throws BWFLAException {
        Component c = getComponent(componentId);
        if( c == null )
            throw new BWFLAException("component not found: " + componentId);
        return c.getComponentType(componentId);
    }

    @Override
    public String getEnvironmentId(String componentId) throws BWFLAException {
        Component c = getComponent(componentId);
        if( c == null )
            throw new BWFLAException("component not found: " + componentId);
        return c.getEnvironmentId(componentId);
    }

    @Override
    public Return getControlUrls(String componentId) throws BWFLAException {
        final SessionRegistry.Entry session = sessions.lookup(componentId);
        final String componentHost = session.getResourceHandle().getNodeID().getNodeAddress();

        Return r = getComponent(componentId).getControlUrls(componentId);
        r.getEntry().replaceAll(e -> {
            final URI orig = URI.create(e.getValue());
            Return.Entry entry = new Return.Entry();
            entry.setKey(e.getKey());
            entry.setValue(ComponentProxy.normalize(orig, componentHost));
            return entry;
        });

        return r;
    }

    @Override
    public String getEventSourceUrl(String componentId) throws BWFLAException {
        final SessionRegistry.Entry session = sessions.lookup(componentId);
        final String componentHost = session.getResourceHandle().getNodeID().getNodeAddress();
        final String orig = this.getComponent(componentId).getEventSourceUrl(componentId);
        return ComponentProxy.normalize(URI.create(orig), componentHost);
    }

    @Override
    public BlobHandle getResult(String componentId) throws BWFLAException {
        return getComponent(componentId).getResult(componentId);
    }

    private static String normalize(URI orig, String componentHost) {
        final URI hostURI = URI.create(componentHost);
        final String host = hostURI.getHost() == null ? componentHost : hostURI.getHost();
        final int port = orig.getPort() < 0 ? hostURI.getPort() : orig.getPort();

        // fallback to http
        String scheme = "http";
        if (orig.getScheme() == null) {
            if (hostURI.getScheme() != null)
                scheme = hostURI.getScheme();
        }
        else {
            if (orig.getScheme().startsWith("ws") && hostURI.getScheme() != null && hostURI.getScheme().startsWith("https")) {
                scheme = "wss";
            }
            else scheme = orig.getScheme();
        }

        try {
            return new URI(scheme, orig.getUserInfo(), host, port, orig.getPath(), orig.getQuery(), orig.getFragment())
                    .normalize()
                    .toString();
        }
        catch (URISyntaxException ex) {
            // this catch clause mimicks the behaviour of URI.create()
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
