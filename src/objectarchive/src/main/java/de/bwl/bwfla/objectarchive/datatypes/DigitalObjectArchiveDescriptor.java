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

package de.bwl.bwfla.objectarchive.datatypes;

import java.io.Serializable;

public class DigitalObjectArchiveDescriptor implements Serializable {

	public enum ArchiveType {FILE, ROSETTA, PRESERVICA, METS, USER,};

	private String name;
	private ArchiveType type;
	private boolean defaultArchive;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArchiveType getType() {
		return type;
	}
	public void setType(ArchiveType type) {
		this.type = type;
	}
	public boolean isDefaultArchive() {
		return defaultArchive;
	}
	public void setDefaultArchive(boolean defaultArchive) {
		this.defaultArchive = defaultArchive;
	}	
}
