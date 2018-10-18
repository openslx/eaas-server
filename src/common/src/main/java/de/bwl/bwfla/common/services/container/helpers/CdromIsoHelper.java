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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.common.services.container.types.CdromContainer;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;

/**
 * @author iv1004
 *
 */
public class CdromIsoHelper extends ContainerHelper 
{	
	protected static final Logger log = Logger.getLogger("CdromIsoHelper");
	/* (non-Javadoc)
	 * @see de.bwl.bwfla.common.services.container.helpers.ContainerHelper#createEmptyContainer()
	 */
	@Override
	public Container createEmptyContainer() 
	{		
		// XXX: not implemented yet
		return new CdromContainer();
	}
	
	public static boolean createIso(File dest, List<File> files)
	{
		File pathnames = null;
		PrintWriter printWriter = null;
		
		try
		{
			pathnames = File.createTempFile("/cdrom_pathnames", ".txt");
			
			printWriter = new PrintWriter(new FileWriter(pathnames.getAbsolutePath(), true));
			
			for(File file: files)
			{
				if(file.getName().startsWith(".fuse"))
					continue;
				if(file.getName().startsWith(".DS"))
					continue;
				
				printWriter.println(file.getAbsolutePath());
				log.info("adding " + file + " to iso");
			}	
			printWriter.flush();
			
			DeprecatedProcessRunner runner = new DeprecatedProcessRunner("mkisofs");
			runner.addArguments("-J", "-r", "-hfs");
			runner.addArguments("-o", dest.getAbsolutePath());
			runner.addArguments("-path-list", pathnames.getAbsolutePath());
			// XXX: --hfs parameter seems to generate warning which sets return code to != 0, have to omit code check
			runner.execute(); 
			
			return dest.isFile();
		}
		catch(IOException e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
		finally
		{
			if(printWriter != null)
				printWriter.close();
			
			if(pathnames != null && pathnames.isFile())
				pathnames.delete();
		}
	}
	
	@Override
	public Container createEmptyContainer(int size) 
	{
		return new CdromContainer();
	}
	
	private static Container createContainerAndInject(List<File> files) 
	{		
		File cdromFile = null;
		CdromContainer cdromContainer = null;

		try
		{
			cdromFile = File.createTempFile("/cdrom_", ".iso");
			if(createIso(cdromFile, files))
			{
				cdromContainer = new CdromContainer();
				cdromContainer.setFile(cdromFile);
			}
		}
		catch(IOException e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		finally
		{
			if(cdromContainer == null && cdromFile != null && cdromFile.isFile())
				cdromFile.delete();
		}
		
		return cdromContainer;
	}
	
	/* (non-Javadoc)
	 * @see de.bwl.bwfla.common.services.container.helpers.ContainerHelper#insertIntoContainer(de.bwl.bwfla.common.services.container.types.Container, java.util.List)
	 */
	@Override
	public boolean insertIntoContainer(Container container, List<File> files) 
	{
		Container buf = createContainerAndInject(files);
		container.setFile(buf.getFile());
		return container.getFile().isFile(); 
	}

	
	/* (non-Javadoc)
	 * @see de.bwl.bwfla.common.services.container.helpers.ContainerHelper#extractFromContainer(de.bwl.bwfla.common.services.container.types.Container)
	 */
	@Override
	public File extractFromContainer(Container container) 
	{
		if(!(container instanceof Container))
			return null;
		
		// XXX: not implemented yet
		return null;
	}
}
