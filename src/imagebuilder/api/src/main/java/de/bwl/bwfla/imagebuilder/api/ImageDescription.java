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

import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.MediumType;
import de.bwl.bwfla.emucomp.api.PartitionTableType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageDescription
{
	@XmlElement(required = true)
	private MediumType medium;

	@XmlElement(required = true)
	private int sizeInMb;

	@XmlElement(required = true)
	private int blocksize;

	@XmlElement(required = false)
	private int partStartBlock;

	@XmlElement(required = true)
	private PartitionTableType partTable;

	@XmlElement(required = true)
	private FileSystemType filesystem;

	@XmlElement(name = "content", required = true)
	private List<ImageContentDescription> entries;

	/** Default size in bytes of an image block */
	public static final int DEFAULT_BLOCK_SIZE = 512;

	/** Default start block for first partition */
	public static final int DEFAULT_PARTITION_START = 2048;


	public ImageDescription()
	{
		this(4);
	}

	public ImageDescription(int capacity)
	{
		this.entries = new ArrayList<ImageContentDescription>(capacity);
		this.setPartitionStartBlock(DEFAULT_PARTITION_START);
		this.setBlockSize(DEFAULT_BLOCK_SIZE);
	}

	public List<ImageContentDescription> getContentEntries()
	{
		return entries;
	}

	public ImageDescription addContentEntry(ImageContentDescription content)
	{
		if (content == null)
			throw new IllegalArgumentException("Invalid image content entry!");

		this.entries.add(content);
		return this;
	}

	public MediumType getMediumType()
	{
		return medium;
	}

	public ImageDescription setMediumType(MediumType medium)
	{
		if (medium == null)
			throw new IllegalArgumentException("Invalid medium type!");

		this.medium = medium;
		return this;
	}

	public ImageDescription setMediumType(String medium)
	{
		return this.setMediumType(MediumType.valueOf(medium.toUpperCase()));
	}

	public int getSizeInMb()
	{
		return sizeInMb;
	}

	public ImageDescription setSizeInMb(int size)
	{
		if (size < 0)
			throw new IllegalArgumentException("Invalid image's size: " + size);

		this.sizeInMb = size;
		return this;
	}

	public int getBlockSize()
	{
		return blocksize;
	}

	public ImageDescription setBlockSize(int size)
	{
		if (size < 0)
			throw new IllegalArgumentException("Invalid image's blocksize: " + size);

		this.blocksize = size;
		return this;
	}

	public int getPartitionStartBlock()
	{
		return partStartBlock;
	}

	public ImageDescription setPartitionStartBlock(int block)
	{
		if (block < 0)
			throw new IllegalArgumentException("Invalid partition's start block: " + block);

		this.partStartBlock = block;
		return this;
	}

	public int getPartitionOffset()
	{
		return partStartBlock * blocksize;
	}

	public PartitionTableType getPartitionTableType()
	{
		return partTable;
	}

	public ImageDescription setPartitionTableType(PartitionTableType table)
	{
		if (table == null)
			throw new IllegalArgumentException("Invalid partition table type!");

		this.partTable = table;
		return this;
	}

	public ImageDescription setPartitionTableType(String table)
	{
		return this.setPartitionTableType(PartitionTableType.valueOf(table.toUpperCase()));
	}

	public FileSystemType getFileSystemType()
	{
		return filesystem;
	}

	public ImageDescription setFileSystemType(FileSystemType filesystem)
	{
		if (filesystem == null)
			throw new IllegalArgumentException("Invalid filesystem type!");

		this.filesystem = filesystem;
		return this;
	}

	public ImageDescription setFileSystemType(String filesystem)
	{
		return this.setFileSystemType(FileSystemType.valueOf(filesystem.toUpperCase()));
	}

	public String toShortSummary()
	{
		return medium.name() + " (" + partTable.name() + "+" + filesystem.name() + ")";
	}
}
