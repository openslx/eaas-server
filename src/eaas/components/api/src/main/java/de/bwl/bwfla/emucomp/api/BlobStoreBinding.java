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

package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "blobStoreBinding", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
		"fileSystemType",
		"offset",
		"resourceType",
		"mountFS"
})
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class BlobStoreBinding extends Binding
{
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
	protected FileSystemType fileSystemType;

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
	private int offset;

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
	private ResourceType resourceType;

	//FIXME. Workaround to differentiate between emulators and containers in BindingsManager
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
	private boolean mountFS = false;


	public BlobStoreBinding()
	{
		this.fileSystemType = null;
		this.offset = -1;
	}

	public BlobStoreBinding(FileSystemType fstype)
	{
		this.fileSystemType = fstype;
	}
	
	public void copy(BlobStoreBinding other)
	{
		this.fileSystemType = other.fileSystemType;
		this.offset = other.offset;
	}

	public FileSystemType getFileSystemType()
	{
		return fileSystemType;
	}

	public void setFileSystemType(FileSystemType fstype)
	{
		this.fileSystemType = fstype;
	}

	public void setFileSystemType(String type)
	{
		this.fileSystemType = FileSystemType.valueOf(type.toUpperCase());
	}

	public int getPartitionOffset()
	{
		return offset;
	}

	public void setPartitionOffset(int offset)
	{
		this.offset = offset;
	}

	public boolean getMountFS() {
		return mountFS;
	}

	public void setMountFS(boolean mountFS) {
		this.mountFS = mountFS;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}
}
