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

package de.bwl.bwfla.emil.session;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.eaas.client.ComponentGroupClient;
import de.bwl.bwfla.emil.datatypes.SessionResource;
import de.bwl.bwfla.emucomp.client.ComponentClient;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.logging.Logger;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Session extends JaxbType {
	private final String id;
	private final String groupId;
	private long expirationTimestamp = 0;
	private boolean detached = false;
	private boolean failed = false;
	private long lastUpdate;

	@XmlElement
	private String name;

	Session(String id, String groupId) {
		this.groupId = groupId;
		this.id = id;
	}

	public String id()
	{
		return id;
	}

	String groupId()
	{
		return groupId;
	}

	void setFailed()
	{
		failed = true;
	}

	void update()
	{
		lastUpdate = System.currentTimeMillis();
	}

	long getExpirationTimestamp()
	{
		return expirationTimestamp;
	}

	void setExpirationTimestamp(long timestamp)
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

	long getLastUpdate() {
		return lastUpdate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}