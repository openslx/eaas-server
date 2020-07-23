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

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.StringWriter;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BlobDescription
{
	@XmlElement(name = "file_extension", required = true)
	private String fileext;

	@XmlElement(name = "filename")
	private String filename;

	@XmlElement(name = "description")
	private String description;

	/** Pattern representing valid filenames */
	private static final String NAME_PATTERN = "\\w([-_]\\w)*";

	/** Pattern representing valid types */
	private static final String EXTENSION_PATTERN = "(\\.[a-zA-Z0-9]+)+";

	private static final String METADATA_NAME_PREFIX = "--BLOBMETA--";

	public static final String METADATA_CONTENT_TYPE = "application/json";


	public BlobDescription()
	{
		this.fileext = null;
		this.filename = null;
		this.description = null;
	}

	public String getFileExtension()
	{
		return fileext;
	}

	public BlobDescription setFileExtension(String extension)
	{
		if (extension == null)
			return this;

		BlobDescription.checkFileExtension(extension);
		this.fileext = extension;
		return this;
	}

	public boolean hasFileName()
	{
		return (filename != null && !filename.isEmpty());
	}

	public String getFileName()
	{
		return filename;
	}

	public BlobDescription setFileName(String name)
	{
		BlobDescription.checkFileName(name);
		this.filename = name;
		return this;
	}

	public boolean hasDescription()
	{
		return (description != null && !description.isEmpty());
	}

	public String getDescription()
	{
		return description;
	}

	public BlobDescription setDescription(String description)
	{
		BlobDescription.check(description, "Blob's description");
		this.description = description;
		return this;
	}

	public String toJsonString() throws IOException
	{
		final StringWriter writer = new StringWriter(4096);
		final ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(writer, this);
		return writer.toString();
	}

	public static String toMetaDataName(String blobname)
	{
		return METADATA_NAME_PREFIX + "/" + blobname;
	}


	// ========== Internal Helpers ====================

	private static void check(String value, String msgprefix)
	{
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(msgprefix + " is null or empty!");
	}

	private static void checkFileName(String name)
	{
		BlobDescription.check(name, "Blob's file name");
		if (!name.matches(NAME_PATTERN))
			throw new IllegalArgumentException("Blob's file name contains invalid character(s)!");
	}

	private static void checkFileExtension(String name)
	{
		BlobDescription.check(name, "Blob's file extension");
		if (!name.matches(EXTENSION_PATTERN))
			throw new IllegalArgumentException("Blob's file extension contains invalid character(s)!");
	}
}
