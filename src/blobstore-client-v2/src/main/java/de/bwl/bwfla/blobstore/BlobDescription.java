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

import io.minio.StatObjectResponse;
import io.minio.messages.Item;

import java.time.ZonedDateTime;
import java.util.Map;


/** Description of a blob */
public class BlobDescription
{
	private final String bucket;
	private final String name;
	private final String etag;
	private final ZonedDateTime mtime;
	private final long size;
	private final String contentType;
	private final Map<String, String> userdata;

	BlobDescription(StatObjectResponse stat)
	{
		this.bucket = stat.bucket();
		this.name = stat.object();
		this.etag = BlobDescription.checkETag(stat.etag());
		this.mtime = stat.lastModified();
		this.size = stat.size();
		this.contentType = stat.contentType();
		this.userdata = stat.userMetadata();
	}

	BlobDescription(String bucket, Item item)
	{
		this.bucket = bucket;
		this.name = item.objectName();
		this.etag = BlobDescription.checkETag(item.etag());
		this.mtime = item.lastModified();
		this.size = item.size();
		this.contentType = "application/octet-stream";
		this.userdata = item.userMetadata();
	}

	/** Blob's bucket name */
	public String bucket()
	{
		return bucket;
	}

	/** Blob's name */
	public String name()
	{
		return name;
	}

	/** Blob's etag */
	public String etag()
	{
		return etag;
	}

	/** Blob's last-modified time */
	public ZonedDateTime mtime()
	{
		return mtime;
	}

	/** Blob's size */
	public long size()
	{
		return size;
	}

	/** Blob's content type */
	public String contentType()
	{
		return contentType;
	}

	/** Blob's user-metadata */
	public Map<String, String> userdata()
	{
		return userdata;
	}

	private static String checkETag(String etag)
	{
		if (etag == null)
			return null;

		// MinIO can return quoted etags for certain operations,
		// hence always remove quotes to be consistent in our code!
		if (etag.startsWith("\""))
			etag = etag.replaceAll("\"", "");

		return etag;
	}
}
