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
// TODO: add exception throwing/handling
package de.bwl.bwfla.common.services.container.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.HddContainer;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.conf.CommonSingleton;


/**
 * @author iv1004
 *
 */
// TODO: add exception/error handling
public class HddFat16Helper extends ContainerHelper 
{
	private static final int DEFAULT_HDD_SIZE = 150;
	private static final int MAX_HDD_SIZE_MB = 500;
	private static final int MIN_HDD_SIZE_MB = 100;

	Logger LOG = Logger.getLogger(this.getClass().getName());

	@Override
	public HddContainer createEmptyContainer()
	{
		return createEmptyContainer(DEFAULT_HDD_SIZE);
	}
	
	public HddContainer createEmptyContainer(int sizeMB)
	{	
		// validating input data
		if (sizeMB < MIN_HDD_SIZE_MB)
			sizeMB = MIN_HDD_SIZE_MB;
		
		if (sizeMB > MAX_HDD_SIZE_MB)
			sizeMB = MAX_HDD_SIZE_MB;	
		
		// acquire hdd_create.sh script location
		File hddCreateScript = new File(CommonSingleton.helpersConf.hddFat16Create); 
		File hddFile = null;
		HddContainer hddContainer = null;
		
		try
		{
			hddFile = File.createTempFile("/hdd_", ".img");

			DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
			runner.setCommand(hddCreateScript.getAbsolutePath());
			runner.addArgument("6");
			runner.addArgument(String.valueOf(sizeMB));
			runner.addArgument(hddFile.toString());

			if(runner.execute() && hddFile.isFile())
			{
				hddContainer = new HddContainer();
				hddContainer.setFile(hddFile);
			}
		}
		catch(IOException e)
		{
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		finally
		{
			if(hddContainer == null && hddFile != null && hddFile.isFile())
			{
				hddFile.delete();
				return null;
			}
		}

		return hddContainer;
	}

	@Override
	public boolean insertIntoContainer(Container container, List<File> files) 
	{
		// validating input
		if (!(container instanceof HddContainer))
			return false;
		
		File hddFile = container.getFile();
		if(!hddFile.exists())
			return false;
		
		// acquire hdd_io.sh script location
		File hddIoScript = new File(CommonSingleton.helpersConf.hddFat16Io);
		DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
	
		// iteratively inject files into hdd
		for (File file: files)
		{
			runner.setCommand(hddIoScript.getAbsolutePath());
	        runner.addArgument("i");
	        runner.addArgument(hddFile.getAbsolutePath());
	        runner.addArgument(file.getAbsolutePath());
	        if (!runner.execute())
	        	return false;
		}
		
		return true;
	}

	
	@Override
	public File extractFromContainer(Container container)
	{
		if (!(container instanceof HddContainer))
			return null;
				
		File hddFile = container.getFile();
		if(!hddFile.exists())
			return null;
		
		File hddIoScript = new File(CommonSingleton.helpersConf.hddFat16Io);
		File result = null;
		boolean success = false;
		
		try
		{
		    result = Files.createTempDirectory("").toFile();

			DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
			runner.setCommand(hddIoScript.getAbsolutePath());
			runner.addArgument("e");
			runner.addArgument(hddFile.getAbsolutePath());
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