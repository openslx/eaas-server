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

package com.openslx.eaas.imagearchive;

import com.openslx.eaas.common.event.ServerStartupEvent;
import com.openslx.eaas.imagearchive.config.ImageArchiveConfig;
import com.openslx.eaas.imagearchive.indexing.IndexRegistry;
import com.openslx.eaas.imagearchive.service.ServiceRegistry;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class ArchiveBackend
{
	private static final Logger LOG = Logger.getLogger("IMAGE-ARCHIVE");

	@Resource(lookup = "java:jboss/ee/concurrency/scheduler/default")
	private ScheduledExecutorService scheduler;

	@Resource(lookup = "java:jboss/ee/concurrency/executor/io")
	private ExecutorService executor;

	private ImageArchiveConfig config;
	private StorageRegistry storage;
	private IndexRegistry indexes;
	private ServiceRegistry services;


	public ScheduledExecutorService scheduler()
	{
		return scheduler;
	}

	public ExecutorService executor()
	{
		return executor;
	}

	public ImageArchiveConfig config()
	{
		return config;
	}

	public StorageRegistry storage()
	{
		return storage;
	}

	public IndexRegistry indexes()
	{
		return indexes;
	}

	public ServiceRegistry services()
	{
		return services;
	}

	/** Return global backend-instance */
	public static ArchiveBackend instance()
	{
		return CDI.current()
				.select(ArchiveBackend.class)
				.get();
	}

	/** Return global logger */
	public static Logger logger()
	{
		return LOG;
	}

	/** Create a new prefix-logger */
	public static Logger logger(String key, String value)
	{
		final var context = new PrefixLoggerContext()
				.add(key, value);

		return new PrefixLogger(LOG.getName(), context);
	}

	/** Return current timestamp */
	public static long now()
	{
		return System.currentTimeMillis();
	}


	// ===== Internal Helpers ==============================

	protected ArchiveBackend()
	{
		// Empty!
	}

	@PostConstruct
	private void initialize()
	{
		try {
			this.config = ImageArchiveConfig.create(LOG);
			this.storage = StorageRegistry.create(config.getStorageConfig());
			this.indexes = IndexRegistry.create()
					.rebuild(storage);

			this.services = ServiceRegistry.create(this);
		}
		catch (Exception error) {
			throw new RuntimeException(error);
		}
	}

	@PreDestroy
	private void destroy()
	{
		ArchiveBackend.close(services);
		ArchiveBackend.close(indexes);
		ArchiveBackend.close(storage);
	}

	private static void close(AutoCloseable service)
	{
		try {
			service.close();
		}
		catch (Exception error) {
			LOG.log(Level.WARNING, "Closing service failed!", error);
		}
	}


	// ===== Lifecycle Hooks ==============================

	private void handle(@Observes @Initialized(ApplicationScoped.class) Object unused)
	{
		// Trigger CDI-based initialization on startup!
		LOG.info("Received event 'application-scope-initialized'");
	}

	private void handle(@Observes ServerStartupEvent event)
	{
		// Trigger CDI-based initialization on startup!
		LOG.info("Received event 'server-startup'");
	}
}
