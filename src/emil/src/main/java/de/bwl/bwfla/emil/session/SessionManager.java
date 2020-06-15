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

import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.session.rest.DetachRequest;
import org.apache.tamaya.inject.api.Config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
public class SessionManager
{
	private final Logger log = Logger.getLogger("SESSION-MANAGER");

	private final Map<String, Session> sessions;

	@Inject
	private Components endpoint = null;

	@Inject
	@Config("components.client_timeout")
	protected Duration sessionExpirationTimeout;


	public SessionManager()
	{
		this.sessions = new ConcurrentHashMap<String, Session>();
	}

	/** Registers a new session */
	public void register(Session session)
	{
		sessions.put(session.id(), session);
		log.info("Session '" + session.id() + "' registered");
	}

	/** Returns session */
	public Session get(String id)
	{
		return sessions.get(id);
	}

	/** Returns a list of all session IDs */
	public Collection<Session> list()
	{
		return sessions.values()
				.stream()
				.filter(Session::isDetached)
				.collect(Collectors.toList());
	}

	/** Updates session's lifetime */
	public void setLifetime(String sid, long lifetime, TimeUnit unit, String name)
	{
		this.setLifetime(sid, lifetime, unit, name, null);
	}

	/** Updates session's lifetime */
	public void setLifetime(String sid, long lifetime, TimeUnit unit, String name, DetachRequest.ComponentTitleCreator title)
	{
		sessions.computeIfPresent(sid, (unused, session) -> {
			session.setName(name);
			if (lifetime < 0) {
				session.setExpirationTimestamp(-1);
			}
			else {
				final long timestamp = SessionManager.timems() + unit.toMillis(lifetime);
				session.setExpirationTimestamp(timestamp);
			}

			if (title != null && title.getComponentName() != null) {
				final String cid = title.getComponentId();
				final Optional<SessionComponent> result = session.components()
						.stream()
						.filter((component) -> cid.contentEquals(component.id()))
						.findFirst();

				if (result.isPresent()) {
					final SessionComponent component = result.get();
					component.setCustomName(title.getComponentName());
				}
				else log.warning("Component " + cid + " not found in session " + sid + "!");
			}

			return session;
		});
	}

	/** Send keepalive for session */
	public boolean keepalive(String id)
	{
		final Session session = this.get(id);
		if (session == null)
			return false;

		session.keepalive(endpoint, log);
		return true;
	}

	/** Runs internal keepalives calls for detached session's resources */
	public void update(ExecutorService executor)
	{
		final List<String> idsToRemove = new ArrayList<String>();
		final long timeout = 2L * sessionExpirationTimeout.toMillis();
		final long curtime = SessionManager.timems();
		sessions.forEach((id, session) -> {
			// Remove stale entries...
			if (curtime > session.getLastUpdate() + timeout) {
				idsToRemove.add(id);
				return;
			}

			if (session.isDetached()) {
				if (session.hasExpirationTimestamp() && curtime > session.getExpirationTimestamp())
					idsToRemove.add(id);
				else executor.execute(new SessionKeepAliveTask(session, log));

				return;
			}
		});

		idsToRemove.forEach((id) -> {
			log.info("Session '" + id + "' expired!");
			this.remove(id);
		});
	}

	/** Remove session */
	public void remove(String id)
	{
		final Session session = this.get(id);
		if (session == null)
			return;

		final Collection<String> components = session.components()
				.stream()
				.map(SessionComponent::id)
				.collect(Collectors.toList());

		this.remove(id, components);
	}

	/** Remove session's components */
	public void remove(String sid, String... components)
	{
		this.remove(sid, Arrays.asList(components));
	}

	/** Remove session's components */
	public void remove(String sid, Collection<String> components)
	{
		sessions.computeIfPresent(sid, (unused, session) -> {
			log.info("Removing " + components.size() + " component(s) from session " + sid + "...");
			for (String cid : components) {
				final boolean found = session.components()
						.removeIf((component) -> cid.equals(component.id()));

				if (!found)
					continue;

				// Release component's resources
				try {
					endpoint.releaseComponent(cid);
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Releasing component " + cid + " from session " + sid + " failed!");
				}
			}

			if (session.components().isEmpty()) {
				// Session is empty and can be removed!
				log.info("Session '" + sid + "' removed");
				return null;
			}

			return session;
		});
	}

	public static long timems()
	{
		return System.currentTimeMillis();
	}


	// ========== Internal Helpers ====================

	private class SessionKeepAliveTask implements Runnable
	{
		private final Session session;
		private final Logger log;

		public SessionKeepAliveTask(Session session, Logger log)
		{
			this.session = session;
			this.log = log;
		}

		@Override
		public void run()
		{
			session.keepalive(endpoint, log);
		}
	}
}
