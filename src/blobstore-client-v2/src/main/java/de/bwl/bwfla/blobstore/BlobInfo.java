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

import io.minio.ObjectStat;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;


public class BlobInfo
{
	private final String name;
	private final ZonedDateTime lastModified;
	private final long size;
	private final String contentType;
	private final Map<String, List<String>> headers;

	BlobInfo(ObjectStat stat)
	{
		this.name = stat.name();
		this.lastModified = stat.createdTime();
		this.size = stat.length();
		this.contentType = stat.contentType();
		this.headers = stat.httpHeaders();
	}

	public String getName()
	{
		return name;
	}

	public ZonedDateTime getLastModified()
	{
		return lastModified;
	}

	public long getSize()
	{
		return size;
	}

	public String getContentType()
	{
		return contentType;
	}

	public Map<String, List<String>> getHeaders()
	{
		return headers;
	}
}
