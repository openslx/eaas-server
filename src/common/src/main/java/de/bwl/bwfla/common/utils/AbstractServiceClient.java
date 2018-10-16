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

package de.bwl.bwfla.common.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.concurrent.FirstAccessComputationFuture;


public abstract class AbstractServiceClient<T extends Service> {
    protected Map<URI, Future<T>> serviceMap = new ConcurrentHashMap<>();
    protected ConcurrentMap<T, ConcurrentMap<Class<?>, Future<Object>>> portMap = new ConcurrentHashMap<T, ConcurrentMap<Class<?>, Future<Object>>>();
    
    protected abstract T createService(URL url);
    protected abstract String getWsdlUrl(String host);
    
    protected T getService(URL url) throws BWFLAException {
        try {
            URI uri = url.toURI();
            
            Future<T> future = serviceMap.computeIfAbsent(uri, u -> new FirstAccessComputationFuture<T>(() -> createService(url)));

            return future.get();
        } catch (URISyntaxException|InterruptedException|ExecutionException e) {
            throw new BWFLAException("Could not create web service endpoint: " + e.getMessage(), e);
        }
    }
    
    protected T getService(String host) throws BWFLAException {
        try {
            URI uri = new URI(getWsdlUrl(host));
            return getService(uri.toURL());
        } catch (MalformedURLException|URISyntaxException e) {
            throw new BWFLAException("Could not create web service endpoint: " + e.getMessage(), e);
        }
    }
    
    public <P> P getPort(URL host, Class<P> port) throws BWFLAException {
        return getPort(getService(host), port);
    }

    public <P> P getPort(String host, Class<P> port) throws BWFLAException {
        return getPort(getService(host), port);
    }
    
    @SuppressWarnings("unchecked")
    public <P> P getPort(Service service, Class<P> port) throws BWFLAException {
        ConcurrentMap<Class<?>, Future<Object>> ports = portMap.computeIfAbsent((T) service, s -> new ConcurrentHashMap<Class<?>, Future<Object>>());

        Future<Object> future = ports.computeIfAbsent(port, p ->
            new FirstAccessComputationFuture<Object>(() -> {
                // Disable timeouts and enable MTOM for large file transfers
                BindingProvider bp = (BindingProvider) service.getPort(port);
                bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
                bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "0");
                bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);
                ((SOAPBinding) bp.getBinding()).setMTOMEnabled(true);
                return bp;
            })
        );
        
        try {
            return port.cast(future.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new BWFLAException("Could not create web service proxy object: " + e.getMessage(), e);
        }
    }
}
