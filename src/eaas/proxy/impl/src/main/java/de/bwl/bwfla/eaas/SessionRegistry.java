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

package de.bwl.bwfla.eaas;

import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.proxy.DirectComponentClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class SessionRegistry
{
	/** Active sessions */
	private final Map<String, Entry> sessions = new ConcurrentHashMap<String, Entry>();

	private AtomicBoolean cancelled = new AtomicBoolean(false);

	@Resource(name = "java:jboss/ee/concurrency/scheduler/default")
	private ScheduledExecutorService scheduler = null;

	@Resource(name = "java:jboss/ee/concurrency/executor/io")
	private ExecutorService executor = null;

	@Inject
	private DirectComponentClient components = null;

	/** Intervall between session-cleanup runs */
	private static final Duration CLEANUP_INTERVAL = Duration.ofSeconds(15);


	public void add(String componentId, ResourceHandle handle, Runnable cleanup)
	{
		this.sessions.put(componentId, new Entry(componentId, handle, cleanup));
	}

	public Entry remove(String componentId)
	{
		return this.sessions.remove(componentId);
	}

	public Entry lookup(String componentId)
	{
		return this.sessions.get(componentId);
	}


	public static class Entry
	{
		private final String componentId;
		private final ResourceHandle handle;
		private final Runnable callback;

		public Entry(String componentId, ResourceHandle handle, Runnable callback)
		{
			this.componentId = componentId;
			this.handle = handle;
			this.callback = callback;
		}

		public String getComponentId()
		{
			return componentId;
		}

		public ResourceHandle getResourceHandle()
		{
			return handle;
		}

		private Runnable getCleanupCallback()
		{
			return callback;
		}
	}


	/* =============== Internal Helpers =============== */

	@PostConstruct
	private void initialize()
	{
		final Runnable trigger = () -> executor.execute(new CleanupTask());
		scheduler.schedule(trigger, CLEANUP_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
	}

	@PreDestroy
	private void destroy()
	{
		this.cancelled.set(true);
	}

	private class CleanupTask implements Runnable
	{
		private final Logger log = Logger.getLogger(CleanupTask.class.getName());

		@Override
		public void run()
		{
			if (cancelled.get())
				return;

			try {
				final Iterator<Entry> iterator = sessions.values().iterator();
				while (iterator.hasNext()) {
					final Entry session = iterator.next();
					if (this.isComponentReachable(session))
						continue;  // Check next component

					final String cid = session.getComponentId();
					log.warning("Component '" + cid + "' is unreachable! Removing it from registry...");

					// Execute cleanup-callback
					final Runnable callback = session.getCleanupCallback();
					if (callback != null) {
						try {
							callback.run();
						}
						catch (Exception error) {
							log.log(Level.WARNING, "Running cleanup-callback for component '" + cid + "' failed!", error);
						}
					}

					iterator.remove();
				}
			}
			finally {
				final Runnable trigger = () -> executor.execute(this);
				scheduler.schedule(trigger, CLEANUP_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
			}
		}

		private boolean isComponentReachable(Entry session)
		{
			try {
				final ResourceHandle resource = session.getResourceHandle();
				final Component component = components.getComponentPort(resource.getNodeID());
				component.getState(session.getComponentId());
			}
			catch (Exception error) {
				return false;
			}

			return true;  // Component seems to be reachable!
		}
	}
}
