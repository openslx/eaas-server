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

package com.openslx.eaas.imagearchive.service;

import com.openslx.eaas.imagearchive.indexing.BlobDescriptor;
import com.openslx.eaas.imagearchive.indexing.BlobIndex;
import com.openslx.eaas.imagearchive.storage.StorageLocation;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.blobstore.Blob;
import de.bwl.bwfla.blobstore.BlobStore;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.UUID;


public abstract class BlobService<T extends BlobDescriptor> extends AbstractService<T>
{
	private final StorageRegistry storage;


	/** Look up blob for given ID */
	public Blob blob(String id) throws BWFLAException
	{
		final var descriptor = this.lookup(id);
		if (descriptor == null)
			return null;

		final var location = this.location(descriptor.location());
		final var path = this.path(location, descriptor.name());
		return location.bucket()
				.blob(path.toString());
	}

	/** Download indexed blob from backend storage */
	public InputStream download(String id) throws BWFLAException
	{
		final var blob = this.blob(id);
		if (blob == null)
			return null;

		// start downloading directly from storage...
		return blob.downloader()
				.download();
	}

	/** Upload a new blob to backend storage */
	public String upload(InputStream data) throws BWFLAException
	{
		return this.upload(data, -1L);
	}

	/** Upload a new blob to backend storage */
	public String upload(InputStream data, long size) throws BWFLAException
	{
		final var id = UUID.randomUUID()
				.toString();

		this.upload(id, data, size);
		return id;
	}

	/** Upload blob with given ID to backend storage */
	public void upload(String id, InputStream data) throws BWFLAException
	{
		this.upload(id, data, -1L);
	}

	/** Upload blob with given ID to backend storage */
	public void upload(String id, InputStream data, long size) throws BWFLAException
	{
		// NOTE: upload given data to blob's storage location if known,
		//       else just use default storage location from config!

		final var descriptor = this.lookup(id);
		final var locname = (descriptor != null) ? descriptor.location()
				: storage.config().getDefaultLocation();

		final var location = this.location(locname);
		final var path = this.path(location, id);
		final var blob = location.bucket()
				.blob(path.toString());

		// upload directly to backend storage...
		blob.uploader()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.stream(data, size)
				.upload();

		try {
			// refresh index with uploaded blob...
			final var description = blob.stat();
			this.index()
					.ingest(description, location);
		}
		catch (Exception error) {
			blob.remove();  // blob seems to be invalid, cleanup!
			throw new BWFLAException("Indexing uploaded blob failed!", error);
		}
	}

	/** Remove indexed blob from backend storage */
	public boolean remove(String id) throws BWFLAException
	{
		final var blob = this.blob(id);
		if (blob == null)
			return false;

		blob.remove();

		// remove blob's record from index
		return super.remove(id);
	}


	// ===== Internal Helpers ==============================

	protected BlobService(StorageRegistry storage, BlobIndex<T> index, IdentifierFilter idfilter)
	{
		super(index, idfilter);
		this.storage = storage;
	}

	/** Find named storage location */
	protected StorageLocation location(String name) throws BWFLAException
	{
		final var location = storage.locations()
				.get(name);

		if (location == null)
			throw new BWFLAException("Blob's location not found: " + name);

		return location;
	}

	/** Construct blob's path for given location */
	protected BlobStore.Path path(StorageLocation location, String blob)
	{
		return location.config()
				.getPathPrefix(this.kind())
				.resolve(blob);
	}
}
