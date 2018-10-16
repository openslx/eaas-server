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
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ByteRange;
import de.bwl.bwfla.common.utils.ByteRangeIterator;
import de.bwl.bwfla.common.utils.FileRangeIterator;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import javax.activation.FileDataSource;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class FileSystemBackend implements IBlobStoreBackend
{
	/** Backend's type */
	public static final String TYPE = "fs";

	private final Logger log = Logger.getLogger(FileSystemBackend.class.getName());

	@Config("blobstore.backend.base_dir")
	private String dbBaseDir = null;

	/** Backend's concurrency guard */
	private final Object guard = new Object();

	/** Simple cache for blob's metadata */
	private final ConcurrentMap<String, Blob> cache;


	public FileSystemBackend()
	{
		final Configuration config = ConfigurationProvider.getConfiguration();
		ConfigurationInjection.getConfigurationInjector().configure(this, config);

		this.cache = new ConcurrentHashMap<String, Blob>();
	}

	/* =============== IBlobStoreBackend Implementation =============== */

	@Override
	public BlobHandle save(BlobDescription blob) throws BWFLAException
	{
		final String id = UUID.randomUUID().toString();
		final BlobStoreEntry entry = new BlobStoreEntry(id, blob);
		final String namespace = entry.getNamespace();
		final String blobRefName = this.toBlobRefName(namespace, id);

		final Path dbEntryDir = this.toBlobEntryDir(namespace, id);
		final Path blobDescPath = this.getBlobDescriptionPath(dbEntryDir);
		final Path blobDataPath = this.getBlobDataPath(dbEntryDir);

		log.info("Saving new blob '" + blobRefName + "'...");

		synchronized (guard) {
			// Create base directories...
			try {
				Files.createDirectories(dbEntryDir);
			}
			catch (Exception error) {
				this.abort("Creating base directories failed!", error);
			}

			// Create entry's files...
			try {
				Files.createFile(blobDescPath);
				Files.createFile(blobDataPath);
			}
			catch (Exception error) {
				this.delete(blobDescPath);
				this.delete(blobDataPath);
				this.abort("Creating entry's files failed!", error);
			}
		}

		try {
			// Write blob's metadata
			try (Writer writer = Files.newBufferedWriter(blobDescPath, StandardCharsets.UTF_8)) {
				BlobStoreEntry.write(entry, writer);
			}
			catch (Exception error) {
				this.abort("Writing blob's description failed!", error);
			}

			// Write blob's data
			try (OutputStream stream = Files.newOutputStream(blobDataPath)) {
				blob.getData().writeTo(stream);
			}
			catch (Exception error) {
				this.abort("Writing blob's data failed!", error);
			}
		}
		catch (BWFLAException error) {
			// Clean up created files!
			synchronized (guard) {
				this.delete(blobDescPath);
				this.delete(blobDataPath);
			}

			throw error;
		}

		log.info("Blob '" + blobRefName + "' saved successfully");

		return new BlobHandle(namespace, id, blob.getAccessToken());
	}

	@Override
	public Blob load(BlobHandle handle) throws BWFLAException
	{
		final String id = handle.getId();

		// Fast path: cached blob!
		{
			final Blob blob = cache.get(id);
			if (blob != null) {
				if (!blob.accessTokenEquals(handle.getAccessToken()))
					this.abort("Invalid access token for ID " + id + "!");

				return blob;
			}
		}

		final String namespace = handle.getNamespace();
		final String blobRefName = this.toBlobRefName(namespace, id);

		final Path dbEntryDir = this.toBlobEntryDir(namespace, id);
		final Path blobDescPath = this.getBlobDescriptionPath(dbEntryDir);
		final Path blobDataPath = this.getBlobDataPath(dbEntryDir);

		log.info("Loading blob '" + blobRefName + "'...");

		synchronized (guard) {
			if (!Files.exists(blobDescPath) || !Files.exists(blobDataPath))
				this.abort("Entry for ID " + id + " not found!");
		}

		Blob blob = null;

		// Load requested blob from the store
		try {
			final BlobStoreEntry entry = BlobStoreEntry.read(blobDescPath);
			if (!entry.accessTokenEquals(handle.getAccessToken()))
				this.abort("Invalid access token for ID " + id + "!");

			blob = entry.toBlob();
			blob.setDataFromFile(blobDataPath);
			blob.setSize(Files.size(blobDataPath));
		}
		catch (Exception error) {
			this.abort("Loading blob's entry failed!", error);
		}

		// Cache blobs on first access, since they
		// probably will be accessed multiple times
		cache.put(id, blob);

		log.info("Blob '" + blobRefName + "' loaded successfully");

		return blob;
	}

	@Override
	public ByteRangeIterator load(Blob blob, List<ByteRange> ranges) throws BWFLAException
	{
		try {
			final FileDataSource source = ((FileDataSource) blob.getData().getDataSource());
			final Path path = source.getFile().toPath();
			return new FileRangeIterator(path, ranges);
		}
		catch (Exception error) {
			this.abort("Loading blob's ranges failed!", error);
		}

		return null;
	}

	@Override
	public void delete(BlobHandle handle) throws BWFLAException
	{
		final String id = handle.getId();
		final String namespace = handle.getNamespace();
		final String blobRefName = this.toBlobRefName(namespace, id);

		final Path dbEntryDir = this.toBlobEntryDir(namespace, id);
		final Path blobDescPath = this.getBlobDescriptionPath(dbEntryDir);
		final Path blobDataPath = this.getBlobDataPath(dbEntryDir);

		log.info("Deleting blob '" + blobRefName + "'...");

		synchronized (guard) {
			if (!Files.exists(blobDescPath) || !Files.exists(blobDataPath))
				this.abort("Entry for ID " + id + " not found!");
		}

		// Delete requested blob from the store
		try {
			final BlobStoreEntry entry = BlobStoreEntry.read(blobDescPath);
			if (!entry.accessTokenEquals(handle.getAccessToken()))
				this.abort("Invalid access token for ID " + id + "!");

			cache.remove(id);

			synchronized (guard) {
				this.delete(blobDescPath);
				this.delete(blobDataPath);
				this.delete(dbEntryDir);
			}
		}
		catch (Exception error) {
			this.abort("Deleting blob's entry failed!", error);
		}

		log.info("Blob '" + blobRefName + "' deleted successfully");
	}

	@Override
	public void cleanup(long maxEntryAge, TimeUnit unit) throws BWFLAException
	{
		// Expected directory layout:
		// <BLOBSTORE-BASE-DIR>/
		//     <namespace-1>/
		//         <entry-1>
		//         ...
		//         <entry-m>
		//     ...
		//     <namespace-n>/
		//         <entry-m+1>
		//         ...
		//         <entry-k>

		final CleanupCounters counters = new CleanupCounters();

		try (Stream<Path> namespaces = Files.list(Paths.get(dbBaseDir))) {
			namespaces.forEach((namespace) -> {
				counters.namespace();
				try (Stream<Path> entries = Files.list(namespace)) {
					entries.forEach((entry) -> {
						counters.entry();
						try {
							final FileTime creationTime = (FileTime) Files.getAttribute(entry, "creationTime");
							final long age = System.currentTimeMillis() - creationTime.to(unit);
							if (age > maxEntryAge) {
								cache.remove(entry.getFileName().toString());
								synchronized (guard) {
									this.delete(entry, true);
								}

								counters.deleted(CleanupCounterType.ENTRY);
							}
						}
						catch (IOException error) {
							// The entry could be deleted by other threads!
							// Ignore possible errors.
						}
					});
				}
				catch (Exception error) {
					// Ignore possible errors!
				}

				if (counters.getNumEntriesPerNamespace() == 0) {
					// Try to delete an empty directory
					synchronized (guard) {
						if (this.delete(namespace))
							counters.deleted(CleanupCounterType.NAMESPACE);
					}
				}
			});
		}
		catch (Exception error) {
			this.abort("Cleanup failed!", error);
		}

		if (counters.getNumEntriesDeleted() + counters.getNumNamespacesDeleted() > 0) {
			final String summary = "Cleanup summary: " + counters.getNumEntriesDeleted() + " out of "
					+ counters.getNumEntries() + " blob(s) removed, " + counters.getNumEntriesLeft() + " left. "
					+ counters.getNumNamespacesDeleted() + " out of " + counters.getNumNamespaces()
					+ " namespace(s) removed, " + counters.getNumNamespacesLeft() + " left";

			log.info(summary);
		}
	}


	/* =============== Internal Helpers =============== */

	private String toBlobRefName(String namespace, String id)
	{
		return (namespace + "/" + id);
	}

	private Path toBlobEntryDir(String namespace, String id)
	{
		return Paths.get(dbBaseDir, namespace, id);
	}

	private Path getBlobDescriptionPath(Path dir)
	{
		return dir.resolve("description.json");
	}

	private Path getBlobDataPath(Path dir)
	{
		return dir.resolve("blob.raw");
	}

	private void abort(String message, Throwable error) throws BWFLAException
	{
		log.log(Level.WARNING, message + "\n", error);
		throw new BWFLAException(message, error);
	}

	private void abort(String message) throws BWFLAException
	{
		log.log(Level.WARNING, message);
		throw new BWFLAException(message);
	}

	private boolean delete(Path path)
	{
		return this.delete(path, false);
	}

	private boolean delete(Path path, boolean recursive)
	{
		try {
			if (!recursive) {
				Files.deleteIfExists(path);
				return true;
			}

			try (Stream<Path> stream = Files.walk(path).sorted(Comparator.reverseOrder())) {
				stream.forEach((subpath) -> {
					try {
						Files.deleteIfExists(subpath);
					}
					catch (IOException error) {
						log.log(Level.WARNING, "Deleting file '" + subpath.toString() + "' failed!\n", error);
					}
				});
			}
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Deleting file '" + path.toString() + "' failed!\n", error);
			return false;
		}

		return true;
	}

	private enum CleanupCounterType
	{
		NAMESPACE,
		ENTRY
	}

	private static class CleanupCounters
	{
		private int numNamespaces = 0;
		private int numNamespacesDeleted = 0;
		private int numEntries = 0;
		private int numEntriesDeleted = 0;
		private int numEntriesPerNamespace = 0;

		public void namespace()
		{
			this.numEntriesPerNamespace = 0;
			this.numNamespaces += 1;
		}

		public void entry()
		{
			this.numEntriesPerNamespace += 1;
			this.numEntries += 1;
		}

		public void deleted(CleanupCounterType type)
		{
			switch (type)
			{
				case NAMESPACE:
					this.numNamespacesDeleted += 1;
					break;

				case ENTRY:
					this.numEntriesDeleted += 1;
					break;
			}
		}

		public int getNumNamespaces()
		{
			return numNamespaces;
		}

		public int getNumNamespacesDeleted()
		{
			return numNamespacesDeleted;
		}

		public int getNumNamespacesLeft()
		{
			return (numNamespaces - numNamespacesDeleted);
		}

		public int getNumEntriesPerNamespace()
		{
			return numEntriesPerNamespace;
		}

		public int getNumEntries()
		{
			return numEntries;
		}

		public int getNumEntriesDeleted()
		{
			return numEntriesDeleted;
		}

		public int getNumEntriesLeft()
		{
			return (numEntries - numEntriesDeleted);
		}
	}
}
