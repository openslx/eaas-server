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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BlobHandle
{
	@XmlElement(name = "bucket", required = true)
	private String bucket;

	@XmlElement(name = "name", required = true)
	private String name;


	public BlobHandle()
	{
		this.bucket = "";
		this.name = "";
	}

	public BlobHandle(String bucket, String name)
	{
		BlobUploader.checkName(name);
		this.bucket = bucket;
		this.name = name;
	}

	public String bucket()
	{
		return bucket;
	}

	public String name()
	{
		return name;
	}

	public boolean isValid()
	{
		return !(bucket.isEmpty() || name.isEmpty());
	}

	public static BlobHandle fromUrl(String url) throws MalformedURLException
	{
		return BlobHandle.fromUrl(new URL(url));
	}

	public static BlobHandle fromUrl(URL url) throws IllegalArgumentException
	{
		// We assume, that a blob URL has the following format:
		// <scheme>://<domain-or-host>/<bucket>/<object-path>...

		final Path path = Paths.get(url.getPath());
		if (path.getNameCount() < 2)
			throw new IllegalArgumentException("Blob's URL is invalid: " + url.toString());

		final String bucket = path.getName(0).toString();
		final String blobname = path.subpath(1, path.getNameCount()).toString();

		return new BlobHandle(bucket, blobname);
	}
}
