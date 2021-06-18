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

package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.PartitionTableType;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.emucomp.api.MediumType;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ComponentWithExternalFilesRequest extends ComponentRequest
{


	@XmlElement(name = "input_data")
	private ArrayList<InputMedium> inputs = new ArrayList<InputMedium>();

	public ArrayList<InputMedium> getInputMedia()
	{
		return inputs;
	}

	public void setInputs(ArrayList<InputMedium> inputs) {
		this.inputs = inputs;
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class InputMedium extends JaxbType
	{
		@XmlElement(name = "type")
		private MediumType type;

		@XmlElement(name = "partition_table_type")
		private PartitionTableType partTableType;

		@XmlElement(name = "filesystem_type")
		private FileSystemType fileSystemType;

		@XmlElement(name = "size_mb")
		private int sizeInMb;

		@XmlElement(name = "destination")
		private String destination;

		@XmlElement(name = "content")
		private ArrayList<FileURL> extfiles = new ArrayList<FileURL>();

		/** NOTE: for internal use only currently! */
		private Collection<FileData> inlfiles = new ArrayList<>();

		public InputMedium()
		{
		}

		public InputMedium(String type, int sizeInMb)
		{
			this.setMediumType(type);
			this.setSizeInMb(sizeInMb);
		}

		public void setMediumType(MediumType type)
		{
			this.type = type;
		}

		public void setMediumType(String type)
		{
			this.type = MediumType.valueOf(type.toUpperCase());
		}

		public MediumType getMediumType()
		{
			return type;
		}

		public void setPartitionTableType(PartitionTableType pttype)
		{
			this.partTableType = pttype;
		}

		public void setPartitionTableType(String pttype)
		{
			this.partTableType = PartitionTableType.valueOf(pttype.toUpperCase());
		}

		public PartitionTableType getPartitiionTableType()
		{
			return partTableType;
		}

		public void setFileSystemType(FileSystemType fstype)
		{
			this.fileSystemType = fstype;
		}

		public void setFileSystemType(String fstype)
		{
			this.fileSystemType = FileSystemType.valueOf(fstype.toUpperCase());
		}

		public FileSystemType getFileSystemType()
		{
			return fileSystemType;
		}

		public void setSizeInMb(int size)
		{
			this.sizeInMb = size;
		}

		public int getSizeInMb()
		{
			return sizeInMb;
		}

		public void setDestination(String destination)
		{
			this.destination = destination;
		}

		public String getDestination()
		{
			return destination;
		}

		public void setExtFiles(ArrayList<FileURL> extfiles)
		{
			this.extfiles = extfiles;
		}

		public ArrayList<FileURL> getExtFiles()
		{
			return extfiles;
		}

		public void setInlineFiles(Collection<FileData> inlfiles)
		{
			this.inlfiles = inlfiles;
		}

		public Collection<FileData> getInlineFiles()
		{
			return inlfiles;
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class BaseFileSource extends JaxbType
	{
		@XmlElement(required = true, name = "action")
		private ImageContentDescription.Action action;

		@XmlElement(name = "compression_format")
		private ImageContentDescription.ArchiveFormat compressionFormat;

		@XmlElement(required = true, name = "name")
		private String name;


		protected BaseFileSource()
		{
			// Empty!
		}

		protected BaseFileSource(String action, String name)
		{
			this.setAction(action.toUpperCase());
			this.setName(name);
		}

		public void setAction(ImageContentDescription.Action action)
		{
			this.action = action;
		}

		@XmlElement
		public void setAction(String action)
		{
			this.action = ImageContentDescription.Action.fromString(action);
		}


		@XmlElement
		public void setCompressionFormat(String compressionFormat)
		{
			this.compressionFormat = ImageContentDescription.ArchiveFormat.fromString(compressionFormat);
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public ImageContentDescription.ArchiveFormat getCompressionFormat()
		{
			return compressionFormat;
		}

		public ImageContentDescription.Action getAction()
		{
			return action;
		}

		public boolean hasName()
		{
			return (name != null && !name.isEmpty());
		}

		public String getName()
		{
			return name;
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class FileURL extends BaseFileSource
	{
		@XmlElement(required = true, name = "url")
		private String url;

		public FileURL()
		{
			super();
		}

		public FileURL(String action, String url, String name)
		{
			super(action, name);
			this.setUrl(url);
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public String getUrl()
		{
			return url;
		}
	}

	/** NOTE: for internal use only currently! */
	public static class FileData extends BaseFileSource
	{
		// TODO: since binary data can't be inlined in json,
		//       should it then be always base64-encoded?
		private byte[] data;

		public FileData()
		{
			super();
		}

		public FileData(String action, byte[] data, String name)
		{
			super(action, name);
			this.setData(data);
		}

		public void setData(byte[] data)
		{
			this.data = data;
		}

		public byte[] getData()
		{
			return data;
		}
	}
}
