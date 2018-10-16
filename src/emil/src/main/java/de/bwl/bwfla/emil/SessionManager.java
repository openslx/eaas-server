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

import de.bwl.bwfla.emil.datatypes.SessionResource;

import javax.enterprise.context.ApplicationScoped;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class SessionManager
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	private final Map<String, Session> sessions;


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

	/** Unregisters a session */
	public void unregister(String id)
	{
		sessions.remove(id);
		log.info("Session '" + id + "' unregistered");
	}

	/** Adds new resources to an existing session */
	public void add(String id, List<SessionResource> resources)
	{
		sessions.computeIfPresent(id, (key, session) -> {
			session.resources().addAll(resources);
			log.info("" + resources.size() + " resource(s) added to session '" + id + "'");
			return session;
		});
	}

	/** Removes resources from an existing session */
	public void remove(String id, List<String> resids)
	{
		sessions.computeIfPresent(id, (unused, session) -> {
			final Set<String> idsToRemove = new HashSet<String>(resids);
			final List<SessionResource> resources = session.resources();
			resources.removeIf((resource) -> idsToRemove.contains(resource.getId()));
			log.info("" + resids.size() + " resource(s) removed from session '" + id + "'");
			return session;
		});
	}

	/** Returns session */
	public Session get(String id)
	{
		return sessions.get(id);
	}

	/** Returns a list of all session IDs */
	public Collection<String> list()
	{
		return sessions.keySet();
	}

	/** Updates session's lifetime */
	public void setLifetime(String id, long lifetime, TimeUnit unit)
	{
		sessions.computeIfPresent(id, (unused, session) -> {
			final long timestamp = SessionManager.timems() + unit.toMillis(lifetime);
			session.setExpirationTimestamp(timestamp);
			return session;
		});
	}

	/** Runs keepalive calls for session's resources */
	public void keepalive(Executor executor)
	{
		final List<String> idsToRemove = new ArrayList<String>();
		final long curtime = SessionManager.timems();
		sessions.forEach((id, session) -> {
			if (curtime > session.getExpirationTimestamp()) {
				idsToRemove.add(id);
				return;
			}

			executor.execute(new SessionKeepAliveTask(session, log));
		});

		idsToRemove.forEach((id) -> {
			log.info("Session '" + id + "' expired!");
			this.unregister(id);
		});
	}


	public static long timems()
	{
		return System.currentTimeMillis();
	}


	private static class SessionKeepAliveTask implements Runnable
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
			for (SessionResource resource : session.resources()) {
				HttpURLConnection connection = null;
				try {
					final URL url = new URL(resource.getKeepaliveUrl());
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("POST");
					connection.setUseCaches(false);
					connection.connect();

					final int code = connection.getResponseCode();
					resource.setFailed(code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_NO_CONTENT);
					if (resource.isFailed()) {
						log.warning("Keepalive failed for '" + session.id() + "/" + resource.getId() + "'!");
					}
				}
				catch (Exception error) {
					final String message = "Performing keepalive for session's resource '"
							+ session.id() + "/" + resource.getId() + "' failed!";

					log.log(Level.WARNING, message, error);
				}
				finally {
					if (connection != null)
						connection.disconnect();
				}
			}
		}
	}
}
