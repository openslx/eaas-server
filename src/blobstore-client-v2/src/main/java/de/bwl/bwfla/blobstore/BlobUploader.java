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


import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


public class BlobUploader
{
	private final BlobStoreClient client;
	private BlobDescription description;
	private String contentType;
	private long contentLength;
	private Object data;

	/** Pattern representing valid blob names */
	private static final String NAME_PATTERN = "\\w([/-]\\w)*";


	public BlobUploader(BlobStoreClient client)
	{
		this.client = client;
		this.contentType = "application/octet-stream";
		this.contentLength = -1L;
	}

	public BlobUploader setDescription(BlobDescription description)
	{
		this.description = description;
		return this;
	}

	public BlobUploader setContentType(String type)
	{
		this.contentType = type;
		return this;
	}

	public BlobUploader setContentLength(long length)
	{
		this.contentLength = length;
		return this;
	}

	public BlobUploader setDataFromFile(Path path)
	{
		this.data = path;
		return this;
	}

	public BlobUploader setData(InputStream stream)
	{
		this.data = stream;
		return this;
	}

	public BlobHandle upload(String bucket, String blobname) throws BWFLAException
	{
		BlobUploader.check(bucket,"Blob's upload bucket");
		BlobUploader.check(description, "Blob's description");
		BlobUploader.check(contentType, "Blob's content-type");
		BlobUploader.check(data, "Blob's data");
		BlobUploader.checkName(blobname);

		// Upload blob's data...
		if (data instanceof Path) {
			final Path path = (Path) data;
			client.put(bucket, blobname, path, contentType);
		}
		else if (data instanceof InputStream) {
			final InputStream stream = (InputStream) data;
			if (contentLength > 0)
				client.put(bucket, blobname, stream, contentLength, contentType);
			else client.put(bucket, blobname, stream, contentType);
		}
		else throw new IllegalArgumentException("Blob's data is not suported!");

		// Upload blob's metadata...
		try {
			final ByteArrayInputStream metadata = new ByteArrayInputStream(description.toJsonString().getBytes());
			final String entryname = BlobDescription.toMetaDataName(blobname);
			client.put(bucket, entryname, metadata, metadata.available(), BlobDescription.METADATA_CONTENT_TYPE);
		}
		catch (IOException error) {
			throw new BWFLAException("Serializing blob's description failed!", error);
		}

		return new BlobHandle(bucket, blobname);
	}


	// ========== Internal Helpers ====================

	private static void check(Object value, String msgprefix)
	{
		if (value == null)
			throw new IllegalArgumentException(msgprefix + " is null!");
	}

	private static void check(String value, String msgprefix)
	{
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(msgprefix + " is null or empty!");
	}

	static void checkName(String name)
	{
		BlobUploader.check(name, "Blob's name");
		if (!name.matches(NAME_PATTERN))
			throw new IllegalArgumentException("Blob's name contains invalid character(s)!");
	}
}
