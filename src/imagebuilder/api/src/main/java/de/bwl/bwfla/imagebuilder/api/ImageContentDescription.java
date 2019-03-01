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

import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URL;
import java.nio.file.Path;


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

	@XmlElement(name = "data", required = true)
	private @XmlMimeType("application/octet-stream") DataHandler data;

	@XmlElement(required = true)
	private String name;

	@XmlElement(required = true)
	private String type;

	@XmlElement(required = false)
	private URL fileURL;

	@XmlElement
	private DockerDataSource dockerDataSource = null;

	@XmlElement(required = true)
	private ArchiveFormat archiveFormat;

	/** Pattern representing valid names and namespaces */
	private static final String NAME_PATTERN = "[\\w.-]+";


	public ImageContentDescription()
	{
		this.action = null;
		this.data = null;
		this.name = null;
	}

	public ImageContentDescription(Action action, DataHandler data)
	{
		this.setAction(action);
		this.setData(data);

		this.name = null;
	}

	public ImageContentDescription(Action action, DataHandler dataHandler, String name)
	{
		this.setAction(action);
		this.setData(dataHandler);
		this.setName(name);
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
		ImageContentDescription.checkName(name);
		this.name = name;
		return this;
	}

	public ArchiveFormat getArchiveFormat() {
		return archiveFormat;
	}

	public ImageContentDescription setArchiveFormat(ArchiveFormat archiveFormat) {
		this.archiveFormat = archiveFormat;
		return this;
	}

	public DataHandler getData()
	{
		return data;
	}

	public ImageContentDescription setData(DataHandler data)
	{
		if (data == null)
			throw new IllegalArgumentException("Image's data is null!");

		this.data = data;
		return this;
	}

	public ImageContentDescription setData(DataSource source)
	{
		if (source == null)
			throw new IllegalArgumentException("Image's data source is null!");

		return this.setData(new DataHandler(source ));
	}

	public ImageContentDescription setDataFromFile(Path path)
	{
		if (path == null)
			throw new IllegalArgumentException("Image's data path is null!");

		return this.setData(new FileDataSource(path.toFile()));
	}

	public ImageContentDescription setDataFromDockerSource(DockerDataSource ds)
	{
		this.dockerDataSource = ds;
		this.archiveFormat = ArchiveFormat.DOCKER;
		return this;
	}

	public DockerDataSource getDockerDataSource() {
		return dockerDataSource;
	}

	public ImageContentDescription setDataFromUrl(URL url)
	{
		if (url == null)
			throw new IllegalArgumentException("Image's data URL is null!");

		return this.setData(new URLDataSource(url));
	}

	public URL getURL() {
		return fileURL;
	}

	public ImageContentDescription setURL(URL fileURL) {
		this.fileURL = fileURL;
		return this;
	}

	/* =============== Public Utils =============== */

	public static void check(String value, String prefix)
	{
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(prefix + " is null or empty!");
	}

	public static void checkName(String name)
	{
		ImageContentDescription.check(name, "Image's name");
		if (!name.matches(NAME_PATTERN))
			throw new IllegalArgumentException("Image's name contains invalid character(s)!");
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class DockerDataSource
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

		/* transient members, only used internally */
		public Path rootfs;

		public Path dockerDir;

		public String[] layers;

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
}
