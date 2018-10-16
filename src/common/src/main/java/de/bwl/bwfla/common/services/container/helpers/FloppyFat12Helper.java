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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.FloppyContainer;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.conf.CommonSingleton;



/**
 * @author iv1004
 *
 */
public class FloppyFat12Helper extends ContainerHelper
{
	Logger LOG = Logger.getLogger(this.getClass().getName());

	@Override
	public Container createEmptyContainer()
	{
		// acquire floppy_create.sh script location
		File floppyCreateScript = new File(CommonSingleton.helpersConf.floppyFat12Create); 

		File floppy = null;
		FloppyContainer floppyContainer = null;
		try
		{
			floppy = File.createTempFile("floppy_", ".img");
			
			DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
			runner.setCommand(floppyCreateScript.getAbsolutePath());
			runner.addArgument(floppy.toString());
	
			if(runner.execute() && floppy.isFile())
			{
				floppyContainer = new FloppyContainer();
				floppyContainer.setFile(floppy);
			}
		}
		catch(IOException e)
		{
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		finally
		{
			if(floppyContainer == null && floppy != null && floppy.isFile())
			{
				floppy.delete();
				return null;
			}
		}
		
		return floppyContainer;
	}

	@Override
	public Container createEmptyContainer(int size)
	{
		return createEmptyContainer();
	}

	@Override
	public boolean insertIntoContainer(Container container, List<File> files)
	{
		// validating input
		if(!(container instanceof FloppyContainer))
			return false;

		File floppyFile = container.getFile();
		if(!floppyFile.exists())
			return false;

		// acquire floppy_io.sh script location
		File floppyIoScript = new File(CommonSingleton.helpersConf.floppyFat12Io);
		DeprecatedProcessRunner runner = new DeprecatedProcessRunner();

		// iteratively inject files into floppy
		for(File file : files)
		{
			runner.setCommand(floppyIoScript.getAbsolutePath());
			runner.addArgument("i");
			runner.addArgument(floppyFile.getAbsolutePath());
			runner.addArgument(file.getAbsolutePath());
			if(!runner.execute())
				return false;
		}

		return true;
	}

	@Override
	public File extractFromContainer(Container container)
	{
		if(!(container instanceof FloppyContainer))
			return null;

		File floppyFile = container.getFile();
		if(!floppyFile.exists())
			return null;

		File floppyIoScript = new File(CommonSingleton.helpersConf.floppyFat12Io);
		File result = null;
		
		boolean success = false;
		try
		{
            result = Files.createTempDirectory("").toFile();
			
			DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
			runner.setCommand(floppyIoScript.getAbsolutePath());
			runner.addArgument("e");
			runner.addArgument(floppyFile.getAbsolutePath());
			runner.addArgument(result.getAbsolutePath());
			success = runner.execute();
		}
	    catch (IOException e)
        {
            // Cannot create temporary directory
            return null;
        }
		finally
		{
			if(!success && result != null && result.isDirectory())
			{
				try
				{
					FileUtils.deleteDirectory(result);
				}
				catch(IOException e)
				{
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
				
				return null;
			}
		}
		
		return result;
	}
}