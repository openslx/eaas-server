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

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.client.ComponentGroupClient;
import de.bwl.bwfla.emil.datatypes.SessionResource;
import de.bwl.bwfla.emucomp.client.ComponentClient;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;


public class Session {
	private final String id;
	private final String groupId;
	private long expirationTimestamp = 0;
	private boolean detached = false;
	private boolean failed = false;
	private long lastUpdate;

	@Inject
	private ComponentClient componentClient;

	@Inject
	private ComponentGroupClient groupClient;

	@Inject
	@Config(value = "ws.eaasgw")
	private String eaasGw;

	Session(String id, String groupId) {
		this.groupId = groupId;
		this.id = id;
	}

	public void addComponent(String componentId) throws BWFLAException {
		groupClient.getComponentGroupPort(eaasGw).add(groupId, componentId);
	}

	public void removeComponent(String componentId) throws BWFLAException {
		groupClient.getComponentGroupPort(eaasGw).remove(groupId, componentId);
	}

	public List<String> getComponents() throws BWFLAException {
		return groupClient.getComponentGroupPort(eaasGw).list(groupId);
	}

	public void keepAlive(Logger log)
	{
		try {
			groupClient.getComponentGroupPort(eaasGw).keepalive(groupId);
			lastUpdate = System.currentTimeMillis();
		} catch (BWFLAException e) {
			failed = true;
			if(log != null)
				log.severe("keepalive failed for group " + groupId + " setting status to failed");
		}
	}

	public String id()
	{
		return id;
	}

	public String resources()
	{
		return groupId;
	}

	public long getExpirationTimestamp()
	{
		return expirationTimestamp;
	}

	public void setExpirationTimestamp(long timestamp)
	{
		this.expirationTimestamp = timestamp;
		detached = true;
	}

	public boolean isDetached() {
		return detached;
	}

	public boolean isFailed() {
		return failed;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}
}