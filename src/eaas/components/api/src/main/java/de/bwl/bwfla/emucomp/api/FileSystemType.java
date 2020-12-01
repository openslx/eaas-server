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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


@XmlEnum
@XmlType(namespace = "http://bwfla.bwl.de/components/datatypes")
public enum FileSystemType
{
	@XmlEnumValue("raw")
	RAW,

	@XmlEnumValue("fat16")
	FAT16,

	@XmlEnumValue("fat32")
	FAT32,

	@XmlEnumValue("vfat")
	VFAT,

	@XmlEnumValue("ntfs")
	NTFS,

	@XmlEnumValue("ext2")
	EXT2,

	@XmlEnumValue("ext3")
	EXT3,

	@XmlEnumValue("ext4")
	EXT4,

	@XmlEnumValue("ext4")
	HFS,

	@XmlEnumValue("iso9660")
	ISO9660;

	public static FileSystemType fromString(String str) {
		for (FileSystemType type : FileSystemType.values()) {
			if (type.name().equalsIgnoreCase(str))
				return type;
		}
		throw new IllegalArgumentException();
	}
}
