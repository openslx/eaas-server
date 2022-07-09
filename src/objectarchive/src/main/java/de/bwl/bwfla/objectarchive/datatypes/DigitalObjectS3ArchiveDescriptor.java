/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.objectarchive.datatypes;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


public class DigitalObjectS3ArchiveDescriptor extends DigitalObjectArchiveDescriptor
{
	private String endpoint;
	private String accessKey;
	private String secretKey;
	private String bucket;
	private String path;

	public DigitalObjectS3ArchiveDescriptor()
	{
		super.setType(ArchiveType.S3);
	}

	public String getEndpoint()
	{
		return endpoint;
	}

	public void setEndpoint(String endpoint)
	{
		this.endpoint = endpoint;
	}

	public String getAccessKey()
	{
		return accessKey;
	}

	public void setAccessKey(String accessKey)
	{
		this.accessKey = accessKey;
	}

	public String getSecretKey()
	{
		return secretKey;
	}

	public void setSecretKey(String secretKey)
	{
		this.secretKey = secretKey;
	}

	public String getBucket()
	{
		return bucket;
	}

	public void setBucket(String bucket)
	{
		this.bucket = bucket;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}


	public static DigitalObjectS3ArchiveDescriptor zeroconf() throws IOException
	{
		final var stream = DigitalObjectS3ArchiveDescriptor.class.getClassLoader()
				.getResourceAsStream("zeroconf-s3-object-archive.json");

		try (stream) {
			final ObjectMapper mapper = new ObjectMapper()
					.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

			return mapper.readValue(stream, DigitalObjectS3ArchiveDescriptor.class);
		}
	}
}
