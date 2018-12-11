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

package de.bwl.bwfla.common.services.net;

import de.bwl.bwfla.common.utils.ByteRange;
import de.bwl.bwfla.common.utils.FileRangeIterator;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class HttpExportServlet extends HttpServlet
{
	private static final Map<String, FileCacheEntry> exportedFileCache = new ConcurrentHashMap<String, FileCacheEntry>();
	protected Logger log = Logger.getLogger(this.getClass().getName());

	@Resource(lookup = "java:jboss/ee/concurrency/scheduler/default")
	protected ScheduledExecutorService scheduler;

	@Resource(lookup = "java:jboss/ee/concurrency/executor/io")
	protected ExecutorService executor;

	@Inject
	@Config("http_export_servlet.file_cache.gc_interval")
	private Duration fileCacheGcInterval = null;

	@Inject
	@Config("http_export_servlet.file_cache.entry_eviction_timeout")
	private Duration fileCacheEntryEvictionTimeout = null;


	public abstract File resolveRequest(String path);

	@Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		this.respond(request, response, false);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		this.respond(request, response, true);
	}

	private void respond(HttpServletRequest request, HttpServletResponse response, boolean sendFileData)
			throws ServletException, IOException
	{
		final File file = this.doResolveRequest(request.getPathInfo());
		if (file == null || !file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// We assume here, that we always export a block-device image!
		final long length = HttpUtils.computeBlockDeviceLength(file.length());

		try {
			if (HttpUtils.hasRangeHeader(request)) {
				final List<ByteRange> ranges = HttpUtils.parseRangeHeader(request, length);
				HttpUtils.prepare(response, ranges, length);
				if (!sendFileData)
					return;

				// Send file's ranges
				try (final FileRangeIterator channels = new FileRangeIterator(file.toPath(), ranges)) {
					final ServletOutputStream output = response.getOutputStream();
					HttpUtils.write(output, channels, length, ranges.size() > 1);
				}
			}
			else {
				HttpUtils.prepare(response, file.getName(), length);
				if (!sendFileData)
					return;

				// Send complete file
				final ServletOutputStream output = response.getOutputStream();
				final long written = Files.copy(file.toPath(), output);

				final int padSize = (int)(length - written);
				if(padSize > 0) {
					byte[] bytes = new byte[padSize];
					Arrays.fill(bytes, (byte) 0);
					output.write(bytes);
				}
			}
		}
		catch (Exception error) {
			final Logger log = Logger.getLogger(this.getClass().getName());
			log.log(Level.WARNING, "Writing HTTP response failed!\n", error);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private File doResolveRequest(String path)
	{
		if (!path.startsWith("/")) {
			log.warning("Invalid request path: " + path);
			return null;
		}

		final FileCacheEntry entry = exportedFileCache.computeIfAbsent(path, (unused) -> {
			final File file = this.resolveRequest(path);
			return (file != null) ? new FileCacheEntry(file) : null;
		});

		if (entry != null) {
			entry.update();
			return entry.file();
		}

		return null;
	}

	private void scheduleFileCacheCleanup(Runnable task)
	{
		final long delay = fileCacheGcInterval.toMillis();
		final Runnable trigger = () -> executor.execute(task);
		scheduler.schedule(trigger, delay, TimeUnit.MILLISECONDS);
	}

	@PostConstruct
	protected void initialize()
	{
		this.scheduleFileCacheCleanup(new FileCacheCleanupTask());
	}


	private static long timems()
	{
		return System.currentTimeMillis();
	}


	private static class FileCacheEntry
	{
		private final AtomicLong timestamp;
		private final File file;

		public FileCacheEntry(File file)
		{
			this.timestamp = new AtomicLong(HttpExportServlet.timems());
			this.file = file;
		}

		public File file()
		{
			return file;
		}

		public long timestamp()
		{
			return timestamp.get();
		}

		public void update()
		{
			timestamp.set(HttpExportServlet.timems());
		}
	}

	private class FileCacheCleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try {
				int numCachedEntries = 0;
				int numEvictedEntries = 0;

				final long maxTimeout = fileCacheEntryEvictionTimeout.toMillis();
				final Iterator<Map.Entry<String, FileCacheEntry>> iter = exportedFileCache.entrySet().iterator();
				while (iter.hasNext()) {
					final Map.Entry<String, FileCacheEntry> entry = iter.next();
					final long elapsed = HttpExportServlet.timems() - entry.getValue().timestamp();
					if (elapsed > maxTimeout) {
						++numEvictedEntries;
						iter.remove();
					}

					++numCachedEntries;
				}

				if (numEvictedEntries > 0)
					log.info(numEvictedEntries + " out of " + numCachedEntries + " entries evicted from file-cache");
			}
			finally {
				HttpExportServlet.this.scheduleFileCacheCleanup(this);
			}
		}
	}
}
