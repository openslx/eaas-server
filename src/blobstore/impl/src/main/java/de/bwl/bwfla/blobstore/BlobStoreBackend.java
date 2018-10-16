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

package de.bwl.bwfla.blobstore;

import de.bwl.bwfla.blobstore.api.Blob;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.api.IBlobStore;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ByteRange;
import de.bwl.bwfla.common.utils.ByteRangeIterator;
import de.bwl.bwfla.configuration.converters.DurationPropertyConverter;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.WithPropertyConverter;
import org.jboss.ejb3.annotation.TransactionTimeout;

import javax.inject.Singleton;
import javax.naming.InitialContext;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


@Singleton
public class BlobStoreBackend implements IBlobStore
{
	private final ScheduledExecutorService scheduler;
	private final ExecutorService executor;
	private final IBlobStoreBackend backend;

	@Config("blobstore.gc_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private Duration gcInterval = null;

	@Config("blobstore.max_entry_age")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private Duration maxEntryAge = null;


	public BlobStoreBackend()
	{
		final Logger log = Logger.getLogger(BlobStoreBackend.class.getName());
		this.scheduler = BlobStoreBackend.lookup("java:jboss/ee/concurrency/scheduler/default", log);
		this.executor = BlobStoreBackend.lookup("java:jboss/ee/concurrency/executor/io", log);

		final Configuration config = ConfigurationProvider.getConfiguration();
		ConfigurationInjection.getConfigurationInjector().configure(this, config);

		final String type = config.get("blobstore.backend.type");
		if (type == null || type.isEmpty()) {
			final String message = "BlobStore's backend configuration is missing!";
			throw new IllegalStateException(message);
		}

		this.backend = BlobStoreBackend.create(type);
		this.schedule(new CleanupTask(), gcInterval.toMillis(), TimeUnit.MILLISECONDS);
	}

	/* =============== IBlobStoreBackend Implementation =============== */

	@Override
	@TransactionTimeout(value = 1, unit = TimeUnit.DAYS)
	public BlobHandle put(BlobDescription description) throws BWFLAException
	{
		return backend.save(description);
	}

	@Override
	public Blob get(BlobHandle handle) throws BWFLAException
	{
		return backend.load(handle);
	}

	public ByteRangeIterator get(Blob blob, List<ByteRange> ranges) throws BWFLAException
	{
		return backend.load(blob, ranges);
	}

	@Override
	public void delete(BlobHandle handle) throws BWFLAException
	{
		backend.delete(handle);
	}


	/* =============== Internal Helpers =============== */

	private static IBlobStoreBackend create(String type)
	{
		switch (type) {
			case FileSystemBackend.TYPE:
				return new FileSystemBackend();

			default: {
				final String message = "Unknown BlobStore's backend type: " + type;
				throw new IllegalArgumentException(message);
			}
		}
	}

	private static <T> T lookup(String name, Logger log)
	{
		try {
			return InitialContext.doLookup(name);
		}
		catch (Exception error) {
			log.log(Level.SEVERE, "Lookup for '" + name + "' failed!", error);
			return null;
		}
	}

	private void schedule(Runnable task, long delay, TimeUnit unit)
	{
		scheduler.schedule(() -> executor.execute(task), delay, unit);
	}

	private class CleanupTask implements Runnable
	{
		public void run()
		{
			try {
				backend.cleanup(maxEntryAge.toMillis(), TimeUnit.MILLISECONDS);
			}
			catch (Exception error) {
				// Ignore it!
			}

			BlobStoreBackend.this.schedule(this, gcInterval.toMillis(), TimeUnit.MILLISECONDS);
		}
	}
}
