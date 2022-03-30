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

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.Components;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Session extends JaxbType
{
	@XmlElement
	private final String id;

	@XmlElement
	private String name;

	private long expirationTimestamp = -1L;
	private long configuredExpirationTimestamp = -1L;
	private boolean detached = false;
	private boolean failed = false;
	private long lastUpdate;

	/** List of component IDs */
	private final Set<SessionComponent> components;


	public Session()
	{
		this(UUID.randomUUID().toString());
	}

	public Session(String id) {
		this.id = id;
		this.lastUpdate = SessionManager.timems();
		this.components = Collections.synchronizedSet(new TreeSet<>());
	}

	public String id()
	{
		return id;
	}

	void setFailed()
	{
		failed = true;
	}

	public boolean hasExpirationTimestamp()
	{
		return configuredExpirationTimestamp > 0L;
	}

	long getExpirationTimestamp()
	{
		return expirationTimestamp;
	}

	long getConfiguredExpirationTime()
	{
		return configuredExpirationTimestamp;
	}

	void setConfiguredExpirationTime(long timestamp)
	{
		this.configuredExpirationTimestamp = timestamp;
		detached = true;
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

	public Set<SessionComponent> components()
	{
		return components;
	}

	public void onTimeout(Components endpoint, Logger log)
	{
		// Empty!
	}

	public void keepalive(Components endpoint, Logger log)
	{
		final Function<SessionComponent, Long> checker = (component) -> {
			try {
				endpoint.keepalive(component.id());
				return 0L;
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Sending keepalive failed for component " + component.id() + "!");
				return 1L;
			}
		};

		final Optional<Long> numfailed = components.stream()
				.map(checker)
				.reduce(Long::sum);

		if (numfailed.isPresent() && numfailed.get() > 0L)
			log.info(numfailed + " out of " + components.size() + " component(s) failed in session " + id + "!");

		this.update();
	}

	private void update()
	{
		this.lastUpdate = SessionManager.timems();
	}
}