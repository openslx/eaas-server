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
import de.bwl.bwfla.blobstore.BlobDescription;
import de.bwl.bwfla.blobstore.BlobStore;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.function.Supplier;


public class BlobIngestors
{
	private static final long MAX_INLINE_DATA_SIZE = 1024L * 1024L;


	public static <T extends BlobDescriptor> IBlobIngestor<T> descriptors(Supplier<T> constructor)
	{
		return (context, blob, location) -> {
			final var descriptor = constructor.get();
			BlobIngestors.prepare(descriptor, context, blob, location);
			BlobIngestors.check(descriptor, context, blob, location);
			BlobIngestors.insert(descriptor, context);
		};
	}

	public static <T extends DataRecord<D>, D> IBlobIngestor<T> records(Supplier<T> constructor)
	{
		return (context, blob, location) -> {
			final var record = constructor.get();
			BlobIngestors.prepare(record, context, blob, location);
			BlobIngestors.check(record, context, blob, location);

			// safety check for inline sizes!
			if (blob.size() > MAX_INLINE_DATA_SIZE)
				throw new IllegalStateException("Inline data is too big!");

			// process inline data...
			final var filter = DataRecord.filter(record.name());
			final var current = context.target()
					.collection()
					.lookup(filter);

			final var etag = (current != null) ? current.etag() : null;
			if (etag != null && etag.equals(record.etag())) {
				// currently indexed record is up-to-date, reuse its data!
				record.setData(current.data());
				context.counters()
						.onBlobReused();
			}
			else {
				// data is missing locally, download it from backing storage!
				final var istream = location.bucket()
						.blob(blob.name())
						.downloader()
						.download();

				try (istream) {
					record.setData(istream);
				}

				context.counters()
						.onBlobDownloaded(blob.size());
			}

			BlobIngestors.insert(record, context, filter);
		};
	}


	// ===== Internal Helpers ==============================

	private static <T extends BlobDescriptor> void check(T descriptor, BlobIngestorContext<T> context,
														 BlobDescription blob, StorageLocation location)
	{
		// NOTE: currently blob paths must be of the form "<configured-prefix>/<name>"
		final var exppath = location.config()
				.getPathPrefix(context.target().kind())
				.resolve(descriptor.name())
				.toString();

		// check blob's full path, relative to bucket...
		final var curpath = blob.name();
		if (!curpath.equals(exppath)) {
			var message = "Invalid blob path! Expected '" + exppath + "', but found '" + curpath + "'!";
			throw new IllegalStateException(message);
		}
	}

	private static <T extends BlobDescriptor> void prepare(T descriptor, BlobIngestorContext<T> context,
														   BlobDescription blob, StorageLocation location)
			throws BWFLAException
	{
		// NOTE: blobstore paths usually contain prefixes, but we need filename only!
		final var filename = BlobStore.path(blob.name())
				.last()
				.toString();

		descriptor.setName(filename);
		descriptor.setEtag(blob.etag());
		descriptor.setLocation(location.name());
		descriptor.setModTime(blob.mtime());

		// inline aliases...
		{
			final var aliasing = context.fetcher()
					.aliasing(filename);

			if (aliasing != null)
				descriptor.setAliases(aliasing.aliases());
		}
	}

	private static <T extends BlobDescriptor> void insert(T descriptor, BlobIngestorContext<T> context)
			throws BWFLAException
	{
		final var filter = BlobDescriptor.filter(descriptor.name());
		BlobIngestors.insert(descriptor, context, filter);
	}

	private static <T extends BlobDescriptor> void insert(T descriptor, BlobIngestorContext<T> context, DocumentCollection.Filter filter)
			throws BWFLAException
	{
		context.buffer()
				.replace(filter, descriptor);
	}
}
