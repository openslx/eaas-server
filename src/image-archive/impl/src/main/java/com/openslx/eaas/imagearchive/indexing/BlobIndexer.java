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

package com.openslx.eaas.imagearchive.indexing;

import com.openslx.eaas.imagearchive.storage.StorageLocation;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;


public class BlobIndexer<T> implements AutoCloseable
{
	private final BlobIndex<T> target;


	public BlobIndexer(BlobIndex<T> target)
	{
		this.target = target;
	}

	public void index(StorageRegistry storage, MetaFetcher fetcher) throws BWFLAException
	{
		this.index(storage, fetcher, false);
	}

	public void index(StorageRegistry storage, MetaFetcher fetcher, boolean parallel) throws BWFLAException
	{
		final var tmpname = target.name() + "-" + StringUtils.random(8);
		final var collection = Index.construct(tmpname, target.clazz(), target.logger());
		try {
			target.preparer()
					.prepare(collection);

			final var context = new BlobIngestorContext<>(target, collection, fetcher);
			BlobIndexer.ingest(context, storage, parallel);
			target.switchto(collection);
		}
		catch (Exception error) {
			// clean up...
			collection.drop();

			if (error instanceof BWFLAException)
				throw (BWFLAException) error;
			else throw new BWFLAException(error);
		}
	}

	@Override
	public void close() throws Exception
	{
		// Empty!
	}


	// ===== Internal Helpers ==============================

	private static <D> void ingest(BlobIngestorContext<D> context, StorageRegistry storage, boolean parallel)
			throws BWFLAException
	{
		final var counters = new Result();
		final var logger = context.target().logger();
		logger.info("Indexing storage locations...");

		// TODO: index each location in parallel!

		// build a single index over all configured storage locations...
		for (StorageLocation location : storage.locations().values()) {
			try {
				final var result = BlobIndexer.ingest(context.fork(), location, parallel);
				counters.insertions += result.getNumInsertions();
				counters.failures += result.getNumFailures();
				if (result.isAborted())
					counters.onAbort();
			}
			catch (Exception error) {
				logger.log(Level.WARNING, "Indexing storage location '" + location.name() + "' failed!", error);
				counters.onAbort();
			}
		}

		if (counters.getNumAborts() == storage.locations().size())
			throw new BWFLAException("Indexing storage locations failed!");

		BlobIndexer.summary(storage, counters, logger);
	}

	private static <D> Result ingest(BlobIngestorContext<D> context, StorageLocation location, boolean parallel)
			throws BWFLAException
	{
		final int MAX_NUM_FAILURES = 5;
		final var result = new Result();

		final var path = location.config()
				.getPathPrefix(context.target().kind());

		// should data be available there?
		if (path == null)
			return result;

		final var prefix = path.toString() + "/";
		final var logger = context.target().logger();
		final var blobs = location.bucket()
				.list(prefix);

		// TODO: index each blob in parallel!
		// TODO: use collection's operation-batching!

		try (blobs) {
			// process each blob stored at given location...
			for (final var iter = blobs.iterator(); iter.hasNext();) {
				try {
					final var blob = iter.next();
					if (prefix.equals(blob.name()))
						continue;  // skip base-dir!

					context.target()
							.ingestor()
							.ingest(context, blob, location);

					result.onInsertion();
				}
				catch (Exception error) {
					logger.log(Level.WARNING, "Processing blob failed!", error);

					result.onFailure();
					if (result.getNumFailures() > MAX_NUM_FAILURES) {
						logger.warning("Processing multiple blobs failed! Aborting early!");
						result.onAbort();
						break;
					}
				}
			}
		}

		BlobIndexer.summary(context, location, result, logger);
		return result;
	}

	private static <T> void summary(BlobIngestorContext<T> context, StorageLocation location, Result result, Logger logger)
	{
		final var message = new StringBuilder(512);
		message.append("Indexed ");
		message.append(result.getNumInsertions());
		message.append(" blob(s) from location '");
		message.append(location.name());
		message.append("'");

		final var counters = context.counters();
		if (counters.getNumBlobsReused() > 0) {
			message.append(", reused ");
			message.append(counters.getNumBlobsReused());
		}

		if (counters.getNumBlobsDownloaded() > 0) {
			message.append(", downloaded ");
			message.append(counters.getNumBlobsDownloaded());
			message.append(" (");
			message.append(StringUtils.toHumanSize(counters.getNumBytesDownloaded()));
			message.append(")");
		}

		if (result.getNumFailures() > 0) {
			message.append(", failed ");
			message.append(result.getNumFailures());
			message.append("!");
		}

		logger.info(message.toString());
	}

	private static <T> void summary(StorageRegistry storage, Result result, Logger logger)
	{
		final var numLocations = storage.locations()
				.size();

		final var message = new StringBuilder(512);
		message.append("Indexed ");
		message.append(result.getNumInsertions());
		message.append(" blob(s) from ");
		message.append(numLocations);
		message.append(" location(s)");
		if (result.getNumFailures() > 0) {
			message.append(", failed ");
			message.append(result.getNumFailures());
			message.append("!");
		}

		logger.info(message.toString());
		if (result.getNumAborts() > 0)
			logger.warning("Indexing " + result.getNumAborts() + " out of " + numLocations + " storage location(s) failed!");
	}

	private static class Result
	{
		private int insertions = 0;
		private int failures = 0;
		private int aborts = 0;


		// ===== Updaters ===============

		public void onInsertion()
		{
			++insertions;
		}

		public void onFailure()
		{
			++failures;
		}

		public void onAbort()
		{
			++aborts;
		}


		// ===== Getters ===============

		public int getNumInsertions()
		{
			return insertions;
		}

		public int getNumFailures()
		{
			return failures;
		}

		public int getNumAborts()
		{
			return aborts;
		}

		public boolean isAborted()
		{
			return aborts > 0;
		}
	}
}
