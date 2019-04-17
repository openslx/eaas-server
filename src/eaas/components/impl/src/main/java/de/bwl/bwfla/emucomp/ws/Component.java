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

package de.bwl.bwfla.emucomp.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;

@MTOM
@WebServlet("/ComponentService/Component")
@WebService(targetNamespace = "http://bwfla.bwl.de/api/emucomp")
public class Component
{	
    @Inject
    NodeManager nodeManager;
    
    @Resource(name="wsContext")
    private WebServiceContext  wsContext;
    
    
    @WebMethod
    public String initialize(String id, String config) throws BWFLAException {
        return nodeManager.allocateComponent(id, config);
    }

    @WebMethod
    public void destroy(String id)
	{
	    nodeManager.releaseComponent(id);
	}
    
    @WebMethod
    public void keepalive(String id) throws BWFLAException {
        nodeManager.keepalive(id);
    }
    
    @WebMethod
    public String getState(String id) throws BWFLAException {
        final AbstractEaasComponent component = nodeManager.getComponentById(id, AbstractEaasComponent.class);
        return component.getState().toString();
    }
    
    @WebMethod
    public String getComponentType(String id) throws BWFLAException {
        final AbstractEaasComponent component = nodeManager.getComponentById(id, AbstractEaasComponent.class);

        return component.getComponentType();
    }
    
    @WebMethod
    public Map<String, URI> getControlUrls(String id) throws BWFLAException {
        final AbstractEaasComponent component = nodeManager.getComponentById(id, AbstractEaasComponent.class);
        
        final ServletContext ctx = (ServletContext)wsContext.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        final String context = ctx.getContextPath() + "/";
        
        return component.getControlUrls().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    try {
                        URI orig = e.getValue();
                        return new URI(orig.getScheme(), orig.getAuthority(),
                                orig.getPath().replace("{context}", context), orig.getQuery(),
                                orig.getFragment()).normalize();
                    } catch (URISyntaxException ex) {
                        throw new IllegalArgumentException(ex.getMessage(), ex);
                    }
                }));
    }

    @WebMethod
    public BlobHandle getResult(String id) throws BWFLAException {
        final AbstractEaasComponent component = nodeManager.getComponentById(id, AbstractEaasComponent.class);
        return component.getResult();
    }
}