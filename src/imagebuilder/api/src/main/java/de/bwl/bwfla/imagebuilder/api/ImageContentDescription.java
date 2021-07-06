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

package de.bwl.bwfla.imagebuilder.api;


import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageContentDescription
{
	@XmlEnum
	@XmlType(namespace = "http://bwfla.bwl.de/components/datatypes")
	public enum Action
	{
		@XmlEnumValue("copy")
		COPY,

		@XmlEnumValue("extract")
		EXTRACT,

		@XmlEnumValue("rsync")
		RSYNC;

		public static Action fromString(String value)
		{
			return Action.valueOf(value.toUpperCase());
		}
	}

	@XmlEnum
	@XmlType(namespace = "http://bwfla.bwl.de/components/datatypes")
	public enum ArchiveFormat
	{
		@XmlEnumValue("zip")
		ZIP,

		@XmlEnumValue("tar")
		TAR,

		@XmlEnumValue("simg")
		SIMG,

		@XmlEnumValue("docker")
		DOCKER;

		public static ArchiveFormat fromString(String value)
		{
			return ArchiveFormat.valueOf(value.toUpperCase());
		}
	}


	@XmlElement(required = true)
	private Action action;

	@XmlElementRefs({
			@XmlElementRef(type = UrlDataSource.class),
			@XmlElementRef(type = FileDataSource.class),
			@XmlElementRef(type = ByteArrayDataSource.class),
			@XmlElementRef(type = DockerDataSource.class),
	})
	private DataSource source;

	@XmlElement(required = true)
	private String name;

	@XmlElement(required = false)
	private String subdir;

	@XmlElement(required = true)
	private ArchiveFormat archiveFormat;

	private boolean strictNameCheck = true;

	/** Pattern representing valid names and namespaces */
	private static final String NAME_PATTERN = "[\\w.-]+";


	public ImageContentDescription()
	{
		this.action = null;
		this.source = null;
		this.name = null;
	}

	public ImageContentDescription(Action action, DataSource source)
	{
		this.setAction(action);
		this.setDataSource(source);

		this.name = null;
	}

	public ImageContentDescription(Action action, DataSource source, String name)
	{
		this.setAction(action);
		this.setDataSource(source);
		this.setName(name);
	}

	public ImageContentDescription disableStrictNameChecks()
	{
		this.strictNameCheck = false;
		return this;
	}

	public Action getAction()
	{
		return action;
	}

	public ImageContentDescription setAction(Action action)
	{
		if (action == null)
			throw new IllegalArgumentException("Image's action is null!");

		this.action = action;
		return this;
	}

	public boolean hasName()
	{
		return (name != null && !name.isEmpty());
	}

	public String getName()
	{
		return name;
	}

	public ImageContentDescription setName(String name)
	{
		ImageContentDescription.checkName(name, strictNameCheck);
		this.name = name;
		return this;
	}

	public String getSubdir() {
		return subdir;
	}

	public ImageContentDescription setSubdir(String subdir) {
		ImageContentDescription.checkName(subdir, strictNameCheck);
		this.subdir = subdir;
		return this;
	}

	public ArchiveFormat getArchiveFormat()
	{
		return archiveFormat;
	}

	public ImageContentDescription setArchiveFormat(ArchiveFormat archiveFormat)
	{
		this.archiveFormat = archiveFormat;
		return this;
	}

	public DataSource getDataSource()
	{
		return source;
	}

	public ImageContentDescription setDataSource(DataSource source)
	{
		if (source == null)
			throw new IllegalArgumentException("Image's data source is null!");

		if (source instanceof DockerDataSource)
			this.archiveFormat = ArchiveFormat.DOCKER;

		this.source = source;
		return this;
	}

	/** This method is broken and will work only for local files! */
	@Deprecated
	public ImageContentDescription setFileDataSource(Path path)
	{
		if (path == null)
			throw new IllegalArgumentException("Image's data path is null!");

		return this.setDataSource(new FileDataSource(path));
	}

	public ImageContentDescription setUrlDataSource(URL url)
	{
		if (url == null)
			throw new IllegalArgumentException("Image's data URL is null!");

		return this.setDataSource(new UrlDataSource(url));
	}

	public ImageContentDescription setByteArrayDataSource(byte[] data)
	{
		return this.setDataSource(new ByteArrayDataSource(data));
	}

	public StreamableDataSource getStreamableDataSource()
	{
		return (StreamableDataSource) source;
	}

	public DockerDataSource getDockerDataSource()
	{
		return (DockerDataSource) source;
	}


	@XmlSeeAlso({
			UrlDataSource.class,
			FileDataSource.class,
			ByteArrayDataSource.class,
			DockerDataSource.class
	})
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static abstract class DataSource
	{
		public final boolean isStreamable()
		{
			return (this instanceof StreamableDataSource);
		}

		public StreamableDataSource streamable() throws BWFLAException
		{
			if (!this.isStreamable())
				throw new BWFLAException("Underlying data source is not streamable!");

			return (StreamableDataSource) this;
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static abstract class StreamableDataSource extends DataSource
	{
		public abstract InputStream openInputStream() throws IOException;
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class UrlDataSource extends StreamableDataSource
	{
		@XmlElement
		private URL url;


		public UrlDataSource()
		{
			// Empty!
		}

		public UrlDataSource(URL url)
		{
			this.url = url;
		}

		public UrlDataSource setUrl(URL url)
		{
			this.url = url;
			return this;
		}

		public URL getUrl()
		{
			return url;
		}

		@Override
		public InputStream openInputStream() throws IOException
		{
			return url.openStream();
		}
	}

	@Deprecated
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class FileDataSource extends StreamableDataSource
	{
		// Use string here, since Path is not JAXB-serializable
		@XmlElement
		private String path;


		public FileDataSource()
		{
			// Empty!
		}

		public FileDataSource(String path)
		{
			this.path = path;
		}

		public FileDataSource(Path path)
		{
			this(path.toString());
		}

		public FileDataSource setPath(String path)
		{
			this.path = path;
			return this;
		}

		public FileDataSource setPath(Path path)
		{
			return this.setPath(path.toString());
		}

		public Path getPath()
		{
			return Paths.get(path);
		}

		public String getPathAsString()
		{
			return path;
		}

		@Override
		public InputStream openInputStream() throws IOException
		{
			return Files.newInputStream(this.getPath());
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class ByteArrayDataSource extends StreamableDataSource
	{
		@XmlElement
		private byte[] data;


		public ByteArrayDataSource()
		{
			// Empty!
		}

		public ByteArrayDataSource(byte[] data)
		{
			if (data == null)
				throw new IllegalArgumentException("Image's data array is null!");

			this.data = data;
		}

		public ByteArrayDataSource setBytes(byte[] data)
		{
			if (data == null)
				throw new IllegalArgumentException("Image's data array is null!");

			this.data = data;
			return this;
		}

		public byte[] getBytes()
		{
			return data;
		}

		@Override
		public InputStream openInputStream() throws IOException
		{
			return new ByteArrayInputStream(data);
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class DockerDataSource extends DataSource
	{
		@XmlElement
		public String imageRef;

		// symbolic tag
		@XmlElement
		public String tag;

		// docker digest
		@XmlElement
		public String digest;

		@XmlElement
		public String imageArchiveHost;

		@XmlElement
    	public ArrayList<String> entryProcesses;

		@XmlElement
		public ArrayList<String> envVariables;

		@XmlElement
		public String workingDir;

		public String version;

		public String emulatorType;

		DockerDataSource()
		{
		}

		public DockerDataSource(String imageRef)
		{
			this.imageRef = imageRef;
			this.tag = "latest";
		}

		public DockerDataSource(String imageRef, String tag)
		{
			this.imageRef = imageRef;
			this.tag = tag;
		}
	}


	/* =============== Public Utils =============== */

	public static void check(String value, String prefix)
	{
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(prefix + " is null or empty!");
	}

	public static void checkName(String name)
	{
		checkName(name, false);
	}

	public static void checkName(String name, boolean strict)
	{
		ImageContentDescription.check(name, "Image's name");
		if (strict && !name.matches(NAME_PATTERN))
			throw new IllegalArgumentException("Image's name contains invalid character(s)!");
	}
}
