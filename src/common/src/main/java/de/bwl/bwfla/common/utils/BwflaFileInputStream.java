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

package de.bwl.bwfla.common.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import de.bwl.bwfla.common.services.container.types.ContainerFile;

public class BwflaFileInputStream extends FileInputStream
{
	private final ContainerFile cfile;
	
	public BwflaFileInputStream(ContainerFile cfile) throws FileNotFoundException
	{
		super(cfile);
		
		this.cfile = cfile;
		cfile.addReference();
	}
	
	@Override
	public int read() throws IOException
	{
		this.check();
		
		return super.read();
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException 
	{
		this.check();
		
		return super.read(b, off, len);
	}
	
	@Override
	public int read(byte b[]) throws IOException 
	{
		this.check();
		
		return super.read(b);
	}
	
	@Override
	public void close() throws IOException
	{
		super.close();
		
		cfile.removeReference();
	}
	
	private void check() throws IOException
	{
		if (cfile.isUsable())
			return;
		
		String message = "The underlying file '" + cfile.getAbsolutePath()
				+ "' is marked as deleted! Aborting read operation.";
		throw new IOException(message);
	}
}