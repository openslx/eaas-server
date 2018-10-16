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

package de.bwl.bwfla.common.services.container.helpers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.HddZipContainer;
import de.bwl.bwfla.common.utils.Zip32Utils;



public class HddZipHelper extends ContainerHelper
{

	Logger log = Logger.getLogger(this.getClass().getName());

	@Override
	public Container createEmptyContainer()
	{
		return new HddZipContainer();
	}

	@Override
	public Container createEmptyContainer(int size)
	{
		return this.createEmptyContainer();
	}

	@Override
	public boolean insertIntoContainer(Container container, List<File> files)
	{
		File zipFile = null;
		boolean success = false;
		
		try
		{
			zipFile = File.createTempFile("/hdd_", ".img");
			zipFile.delete();
			
			for(File file: files)
				Zip32Utils.zip(zipFile, file);
			
			container.setFile(zipFile);
			success = true;
		}
		catch(IOException e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		finally
		{
			if(!success && zipFile != null)
				zipFile.delete();
		}
		
		return success;
	}

	@Override
	public File extractFromContainer(Container container)
	{
		// TODO: currently unsupported
		return null;
	}
}