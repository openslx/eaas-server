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
import java.util.List;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.ImageFileContainer;

/**
 * @author mme
 *
 */
public class ImageFileHelper extends ContainerHelper 
{
	/* (non-Javadoc)
	 * @see de.bwl.bwfla.common.services.container.helpers.ContainerHelper#createEmptyContainer()
	 */
	@Override
	public Container createEmptyContainer() 
	{
		return new ImageFileContainer();
	}
	
	@Override
	public Container createEmptyContainer(int size) 
	{
		return new Container();
	}
	
	public Container createContainerAndInject(List<File> files) 
	{		
		if(files.size() != 1)
			return null;

		Container container = new ImageFileContainer();
		container.setFile(files.get(0));
		return container;
	}
	
	
	/* (non-Javadoc)
	 * @see de.bwl.bwfla.common.services.container.helpers.ContainerHelper#insertIntoContainer(de.bwl.bwfla.common.services.container.types.Container, java.util.List)
	 */
	@Override
	public boolean insertIntoContainer(Container container, List<File> files) 
	{
		Container buf = createContainerAndInject(files);
		container.setFile(buf.getFile());
		
		return true;
	}

	
	/* (non-Javadoc)
	 * @see de.bwl.bwfla.common.services.container.helpers.ContainerHelper#extractFromContainer(de.bwl.bwfla.common.services.container.types.Container)
	 */
	@Override
	public File extractFromContainer(Container container) 
	{
		return null;	
	}
}
