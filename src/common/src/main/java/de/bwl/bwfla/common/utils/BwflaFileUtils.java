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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;



public class BwflaFileUtils
{
	private static final long TMP_FILES_SIZE_LIMIT	= (100L) * 1024L * 1024L * 1024L;
	protected static final Logger log = Logger.getLogger("BwflaFileUtils");
	
	// FIXME: implement appending and not rewriting
	public static int appendStreamBytesToFile(InputStream in, File fl, int count) throws IOException
	{	
		File parentDir = fl.getParentFile();
		
		final byte[] buffer = new byte[10 * 1024];
		final long SPACE_THRESHOLD = (30) * 1024 * 1024;
		
		try(OutputStream out = new FileOutputStream(fl.getAbsolutePath(), true))
		{
			int bytesRead = 0;
			int totalBytesRead = 0;
			
			while(totalBytesRead <= count && (bytesRead = in.read(buffer)) != -1)
		    {
				totalBytesRead += bytesRead;

				long freeSpace = (parentDir.getUsableSpace() - bytesRead) - SPACE_THRESHOLD; 
					
				if(freeSpace < 0)
					throw new IOException("no free space left (wrt. to SPACE_THRESHOLD) on partition where the following file is located: "  + fl.getAbsolutePath());
					
				out.write(buffer, 0, bytesRead);
			}
			
			out.flush();
			
			return totalBytesRead;
		}
	}
	
	public static boolean waitForFile(String filename, int retries, long timeout)
	{
		Path file = Paths.get(filename);
		while (--retries >= 0) {

			if (Files.exists(file))
				return true;

			try {
				Thread.sleep(timeout);
			}
			catch (InterruptedException e) {
				break;
			}
		}

		return false;
	}
	
	public static File streamToTmpFile(File tempDir, InputStream istream, String name) throws IOException
	{
		File tmpfile = File.createTempFile(name, null,tempDir);
		FileOutputStream ostream = new FileOutputStream(tmpfile);
		
		try {
			IOUtils.copy(istream, ostream);
			ostream.flush();
			ostream.getFD().sync();
		}
		catch (IOException exception) {
			// Something gone wrong! Retries are not possible here,
			// since we don't know much about the stream's state.
			final String filename = tmpfile.getAbsolutePath();
			log.severe("Writing input-data to temporary file '" + filename + "' failed!");
			log.info("Removing incomplete file '" + filename + "'");
			tmpfile.delete();
			throw exception;
		}
		finally {
			ostream.close();
		}
		
		return tmpfile;
	}
	
	public static File fileToTmpDir(File tempDir, File img)
	{
		if(img == null || !img.exists())
			return null;

		File result = null;
		try
		{
			InputStream in = new FileInputStream(img);
			result = BwflaFileUtils.streamToTmpFile(tempDir, in, img.getName());
			in.close();
		}
		catch(IOException e)
		{
			log.severe(e.getMessage());
		}
		return result;
	}
}