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

/**
 * 
 */
package de.bwl.bwfla.common.services.container.helpers;

import de.bwl.bwfla.common.services.container.types.CdromContainer;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.Container.Filesystem;
import de.bwl.bwfla.common.services.container.types.FloppyContainer;
import de.bwl.bwfla.common.services.container.types.HddContainer;
import de.bwl.bwfla.common.services.container.types.HddHfsContainer;
import de.bwl.bwfla.common.services.container.types.HddZipContainer;
import de.bwl.bwfla.common.services.container.types.ImageFileContainer;

/**
 * @author iv1004
 *
 */
public class ContainerHelperFactory 
{	
	public static ContainerHelper getContainerHelper(String dev, Filesystem fsys)
	{	
		if(dev.equalsIgnoreCase("disk"))
			if(fsys == Filesystem.HFS)
				return new HddHfsHelper();
			else if(fsys == Filesystem.FAT16)
				return new HddFat16Helper();
			else if(fsys == Filesystem.ZIP)
				return new HddZipHelper();
		
		if(dev.equalsIgnoreCase("floppy"))
			return new FloppyFat12Helper();
		
		if(dev.equalsIgnoreCase("cdrom"))
			return new CdromIsoHelper();
		
		if(dev.equalsIgnoreCase("cdrom-image") || dev.equalsIgnoreCase("disk-image") || dev.equalsIgnoreCase("floppy-image"))
			return new ImageFileHelper();
		
		return null;
	}
	
	public static ContainerHelper getContainerHelper(Container container)
	{	
		if(container instanceof HddZipContainer)
			return new HddZipHelper();
		
		if(container instanceof HddHfsContainer)
			return new HddHfsHelper();
		
		if(container instanceof HddContainer)
			return new HddFat16Helper();
		
		if(container instanceof FloppyContainer)
			return new FloppyFat12Helper();
		
		if(container instanceof CdromContainer)
			return new CdromIsoHelper();
		
		if(container instanceof ImageFileContainer)
			return new ImageFileHelper();
		
		return null;
	}
}
