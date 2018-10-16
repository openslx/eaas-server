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

package de.bwl.bwfla.common.services.container.types;

import java.io.File;

// TODO: should devices and labels be separated?
public class Container
{
	public static enum Filesystem {FAT12, FAT16, HFS, ISO, ZIP};
	
	protected ContainerFile cfile;
	
	public ContainerFile getFile()
	{
		return cfile;
	}
	
	public void setFile(File file)
	{
		this.cfile = new ContainerFile(file);
	}
	
	public void setFile(ContainerFile file)
	{
		this.cfile = file;
	}
}
