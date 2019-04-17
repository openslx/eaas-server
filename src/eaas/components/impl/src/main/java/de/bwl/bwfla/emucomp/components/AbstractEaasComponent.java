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

package de.bwl.bwfla.emucomp.components;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.ClusterComponent;
import de.bwl.bwfla.emucomp.api.ComponentState;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;


/** Base class for beans representing EaasComponents */
public abstract class AbstractEaasComponent implements ClusterComponent
{
	private String componentID;
	private String detachedTitle;
	
	protected ComponentState state = ComponentState.OK;
	private BWFLAException asyncError = null;
	
	private Map<String, IConnector> controlConnectors = new ConcurrentHashMap<String, IConnector>();
	
	private final AtomicLong keepaliveTimestamp = new AtomicLong(0);

	/** Result of an async-computation */
	protected final CompletableFuture<BlobHandle> result = new CompletableFuture<BlobHandle>();

	public String getComponentId()
	{
		return componentID;
	}
	
	public void setComponentId(String id)
	{
		this.componentID = id;
	}
	
	@Override
	public ComponentState getState() throws BWFLAException {
	    if (this.asyncError != null) {
	        throw this.asyncError;
	    }
	    return this.state;
	}

	protected URI getComponentResource()
	{
		return URI.create(String.format("%%7Bcontext%%7D/components/%s/", componentID));
	}

	@Override
	public Map<String, URI> getControlUrls() {
	    URI componentResource = getComponentResource();

        return controlConnectors.entrySet().stream().collect(
                Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().getControlPath(componentResource)));
	}

	@Override
	public void setKeepaliveTimestamp(long timestamp)
	{
		keepaliveTimestamp.set(timestamp);
	}

	@Override
	public long getKeepaliveTimestamp()
	{
		return keepaliveTimestamp.get();
	}

	@Override
	public BlobHandle getResult() throws BWFLAException
	{
		try {
			return (result.isDone()) ? result.get() : null;
		}
		catch (Exception error) {
			throw new BWFLAException("Retrieving the async-result failed!", error);
		}
	}

	protected void addControlConnector(IConnector connector) {
	    this.controlConnectors.put(connector.getProtocol(), connector);
	}

	public String getDetachedTitle() {
		return detachedTitle;
	}

	public void setDetachedTitle(String detachedTitle) {
		this.detachedTitle = detachedTitle;
	}

	public IConnector getControlConnector(String protocol) {
	    return controlConnectors.get(protocol);
	}
	
	protected void fail(BWFLAException e) {
	    this.asyncError = e;
	}
}
