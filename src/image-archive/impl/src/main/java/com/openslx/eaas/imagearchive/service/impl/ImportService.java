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

package com.openslx.eaas.imagearchive.service.impl;

import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.BlobKind;
import com.openslx.eaas.imagearchive.config.ImporterConfig;
import com.openslx.eaas.imagearchive.databind.ImportFailure;
import com.openslx.eaas.imagearchive.databind.ImportRecord;
import com.openslx.eaas.imagearchive.databind.ImportSource;
import com.openslx.eaas.imagearchive.databind.ImportStatus;
import com.openslx.eaas.imagearchive.databind.ImportTarget;
import com.openslx.eaas.imagearchive.databind.ImportTask;
import com.openslx.eaas.imagearchive.indexing.impl.ImportIndex;
import com.openslx.eaas.imagearchive.service.BlobService;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.ImageInformation;
import de.bwl.bwfla.common.utils.TaskStack;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class ImportService
{
	private final Logger logger;
	private final AtomicInteger idgen;
	private final Queue<Integer> tasks;
	private final List<IPreprocessor> preprocessors;
	private final Map<Integer, CompletableFuture<ImportStatus>> watchers;
	private final ArchiveBackend backend;
	private final ImporterConfig config;
	private final ImportIndex imports;
	private int numIdleWorkers;


	/** Count currently indexed import-tasks */
	public long count()
	{
		return imports.collection()
				.count();
	}

	/** List all indexed import-tasks within given range */
	public Stream<Integer> list(int offset, int limit) throws BWFLAException
	{
		return imports.collection()
				.list()
				.skip(offset)
				.limit(limit)
				.stream()
				.map(ImportRecord::taskid);
	}

	/** Submit new import-task and return task's ID */
	public int submit(ImportTask config) throws BWFLAException
	{
		final int taskid = idgen.getAndIncrement();
		imports.collection()
				.insert(ImportRecord.create(taskid, config));

		if (!this.submit(taskid)) {
			this.abort(taskid);
			return -1;
		}

		return taskid;
	}

	/** Abort import-task */
	public void abort(int taskid)
	{
		try {
			imports.collection()
					.delete(ImportRecord.filter(taskid));
		}
		catch (Exception error) {
			logger.log(Level.WARNING, "Aborting task " + taskid + " failed!", error);
		}

		final var watcher = watchers.remove(taskid);
		if (watcher != null && !watcher.isDone())
			watcher.complete(ImportStatus.aborted(taskid));
	}

	/** Look up task's current status */
	public ImportStatus lookup(int taskid) throws BWFLAException
	{
		final var record = imports.collection()
				.lookup(ImportRecord.filter(taskid));

		if (record == null)
			return null;

		return ImportStatus.from(record);
	}

	/** Fetch multiple statuses within given range */
	public Stream<ImportStatus> fetch(int offset, int limit) throws BWFLAException
	{
		return imports.collection()
				.list()
				.skip(offset)
				.limit(limit)
				.stream()
				.map(ImportStatus::from);
	}

	/** Register a watcher for task's result */
	public CompletableFuture<ImportStatus> watch(int taskid)
	{
		final Function<Integer, CompletableFuture<ImportStatus>> constructor = (tid) -> {
			try {
				final var record = imports.collection()
						.lookup(ImportRecord.filter(tid));

				if (record == null)
					return null;

				if (record.finished() || record.failed())
					return CompletableFuture.completedFuture(ImportStatus.from(record));

				return new CompletableFuture<>();
			}
			catch (Exception error) {
				throw new RuntimeException(error);
			}
		};

		final var result = watchers.computeIfAbsent(taskid, constructor);
		if (result != null && result.isDone())
			watchers.remove(taskid);

		return result;
	}

	public static ImportService create(ArchiveBackend backend) throws BWFLAException
	{
		final var config = backend.config()
				.getImporterConfig();

		final var index = backend.indexes()
				.imports();

		return new ImportService(config, index, backend);
	}


	// ===== Internal Helpers ==============================

	private ImportService(ImporterConfig config, ImportIndex index, ArchiveBackend backend) throws BWFLAException
	{
		this.logger = ArchiveBackend.logger("service", "imports");
		this.backend = backend;
		this.config = config;
		this.imports = index;
		this.idgen = new AtomicInteger(1 + index.lastid());
		this.tasks = new LinkedList<>();
		this.preprocessors = new ArrayList<>(BlobKind.count());
		this.watchers = new ConcurrentHashMap<>();
		this.numIdleWorkers = config.getNumWorkers();

		// initialize all preprocessors
		for (int i = 0; i < BlobKind.count(); ++i)
			preprocessors.add(null);

		this.insert(BlobKind.IMAGE, new ImagePreprocessor());

		final var executor = backend.executor();
		executor.execute(new CleanupTask());
		executor.execute(this::resume);
	}

	private void insert(BlobKind kind, IPreprocessor preprocessor)
	{
		preprocessors.set(kind.ordinal(), preprocessor);
	}

	private synchronized boolean submit(int taskid)
	{
		if (!tasks.offer(taskid))
			return false;

		if (Math.min(numIdleWorkers, tasks.size()) > 0) {
			backend.executor()
					.execute(new Worker());

			--numIdleWorkers;
		}

		return true;
	}

	private synchronized int poll()
	{
		if (tasks.isEmpty()) {
			++numIdleWorkers;
			return -1;
		}

		return tasks.remove();
	}

	private void resume()
	{
		try {
			final var records = imports.collection()
					.find(ImportRecord.pending());

			try (records) {
				final var count = records.stream()
						.map(ImportRecord::taskid)
						.peek(this::submit)
						.count();

				if (count > 0)
					logger.info("Resumed " + count + " pending import(s)");
			}
		}
		catch (Exception error) {
			logger.log(Level.WARNING, "Resuming pending imports failed!", error);
		}
	}

	@FunctionalInterface
	private interface IPreprocessor
	{
		void prepare(ImportRecord record, TaskStack cleanups) throws Exception;
	}

	private class ImagePreprocessor implements IPreprocessor
	{
		@Override
		public void prepare(ImportRecord record, TaskStack cleanups) throws Exception
		{
			final var task = record.task();
			final var source = task.source();
			final var info = new ImageInformation(source.url(), logger);
			switch (info.getFileFormat()) {
				case VMDK:
				case VHD:
					final var tmpfile = config.getTempDirectory()
							.resolve("image-" + record.taskid());

					cleanups.push("temp-import-image", () -> Files.deleteIfExists(tmpfile));

					// convert remote or local image, storing result locally
					final var format = ImageInformation.QemuImageFormat.QCOW2;
					EmulatorUtils.convertImage(source.url(), tmpfile, format, logger);

					// converted image should now be available locally,
					// so we need to update import-source accordingly!
					source.setUrl("file://" + tmpfile.toString());
			}
		}
	}

	private class Worker implements Runnable
	{
		private final ImportService self = ImportService.this;

		@Override
		public void run()
		{
			int taskid;
			while ((taskid = self.poll()) > 0) {
				try {
					this.execute(taskid);
				}
				catch (Exception error) {
					logger.log(Level.WARNING, "Executing import-task " + taskid + " failed!", error);
				}
			}
		}

		private void execute(int taskid) throws Exception
		{
			final var filter = ImportRecord.filter(taskid);
			final var record = imports.collection()
					.lookup(filter);

			if (record == null)
				return;  // task seems to be aborted!

			record.setStartedAtTime(ArchiveBackend.now());

			final var cleanups = new TaskStack(logger);
			try {
				this.prepare(record, cleanups);
				this.process(record, cleanups);
			}
			catch (Exception error) {
				logger.log(Level.WARNING, "Executing import-task " + taskid + " failed!", error);
				final var failure = new ImportFailure()
						.setReason("Importing blob failed!")
						.setDetail(error.getMessage());

				record.setFailure(failure);
			}
			finally {
				if (!cleanups.execute())
					logger.warning("Cleaning up after import-task failed!");
			}

			record.setFinishedAtTime(ArchiveBackend.now());
			imports.collection()
					.replace(filter, record);

			final var watcher = watchers.remove(record.taskid());
			if (watcher != null)
				watcher.complete(ImportStatus.from(record));
		}

		private void prepare(ImportRecord record, TaskStack cleanups) throws Exception
		{
			final var kind = record.task()
					.target()
					.kind();

			final var preprocessor = preprocessors.get(kind.ordinal());
			if (preprocessor != null)
				preprocessor.prepare(record, cleanups);
		}

		private void process(ImportRecord record, TaskStack cleanups) throws Exception
		{
			final var task = record.task();
			final var source = task.source();
			final var target = task.target();
			final var uri = new URI(source.url());
			switch (uri.getScheme()) {
				case "http":
				case "https":
					this.webdata(uri, source, target);
					break;
				case "file":
					this.filedata(uri, source, target);
					break;
				default:
					throw new IllegalArgumentException("Unsupported source: " + source.url());
			}
		}

		private void filedata(URI uri, ImportSource source, ImportTarget target)
				throws BWFLAException, IOException
		{
			final var file = Path.of(uri).normalize();
			if (!file.startsWith(config.getBaseDirectory()) && !file.startsWith(config.getTempDirectory()))
				throw new IllegalArgumentException("Invalid file path!");

			final var size = Files.size(file);
			try (final var data = Files.newInputStream(file)) {
				this.upload(target, data, size);
			}
		}

		private void webdata(URI uri, ImportSource source, ImportTarget target)
				throws BWFLAException, IOException
		{
			final var process = new DeprecatedProcessRunner()
					.setLogger(logger)
					.setCommand("curl")
					.addArgument("--fail")
					.addArgument("--globoff")
					.addArgument("--silent")
					.addArgument("--show-error")
					.addArgument("--location");

			final var headers = source.headers();
			if (headers != null) {
				headers.forEach((name, value) -> {
					process.addArgument("--header");
					process.addArgument(name + ": " + value);
				});
			}

			// let curl write to stdout!
			process.addArgument("--output")
					.addArgument("-")
					.addArgument(uri.toString());

			try {
				if (!process.start(false))
					throw new BWFLAException("Starting download failed!");

				try (final var data = process.getStdOutStream()) {
					this.upload(target, data, -1L);
				}

				process.waitUntilFinished();
				process.printStdErr();
			}
			finally {
				process.cleanup();
			}
		}

		private void upload(ImportTarget target, InputStream data, long size) throws BWFLAException
		{
			final var service = (BlobService<?>) backend.services()
					.lookup(target.kind());

			if (target.name() != null)
				service.upload(target.location(), target.name(), data, size);
			else target.setName(service.upload(target.location(), data, size));
		}
	}

	private class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try {
				final var count = this.cleanup();
				if (count > 0)
					logger.info("Removed " + count + " expired import record(s)");
			}
			catch (Exception error) {
				logger.log(Level.WARNING, "Running cleanup failed!", error);
			}

			final Runnable trigger = () -> {
				backend.executor()
						.execute(new CleanupTask());
			};

			final var interval = config.getGcInterval();
			backend.scheduler()
					.schedule(trigger, interval.toMillis(), TimeUnit.MILLISECONDS);
		}

		private int cleanup() throws Exception
		{
			final var timestamp = ArchiveBackend.now() - this.maxage();
			final var filter = ImportRecord.expired(timestamp);
			return (int) imports.collection()
					.delete(filter, true);
		}

		private long maxage()
		{
			return config.getMaxRecordAge()
					.toMillis();
		}
	}
}
