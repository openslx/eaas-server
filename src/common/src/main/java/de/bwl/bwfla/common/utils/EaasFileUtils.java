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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;



public class EaasFileUtils
{
	private static final long TMP_FILES_SIZE_LIMIT	= (100L) * 1024L * 1024L * 1024L;
	protected static final Logger log = Logger.getLogger("BwflaFileUtils");
	
	
	public static File createTempDirectory(String suffix) throws IOException
	{
		String tmpSuffix;

		if (suffix == null)
			tmpSuffix = "";
		else
			tmpSuffix = suffix;

		Set<PosixFilePermission> permissions = new HashSet<>();
		permissions.add(PosixFilePermission.OWNER_READ);
		permissions.add(PosixFilePermission.OWNER_WRITE);
		permissions.add(PosixFilePermission.OWNER_EXECUTE);
		permissions.add(PosixFilePermission.GROUP_READ);
		permissions.add(PosixFilePermission.GROUP_WRITE);
		permissions.add(PosixFilePermission.GROUP_EXECUTE);

		return Files.createTempDirectory(tmpSuffix, PosixFilePermissions.asFileAttribute(permissions)).toFile();
	}

	public static Path createTempDirectory(Path basedir, String suffix) throws IOException
	{
		String tmpSuffix;

		if (suffix == null)
			tmpSuffix = "";
		else
			tmpSuffix = suffix;

		Set<PosixFilePermission> permissions = new HashSet<>();
		permissions.add(PosixFilePermission.OWNER_READ);
		permissions.add(PosixFilePermission.OWNER_WRITE);
		permissions.add(PosixFilePermission.OWNER_EXECUTE);
		permissions.add(PosixFilePermission.GROUP_READ);
		permissions.add(PosixFilePermission.GROUP_WRITE);
		permissions.add(PosixFilePermission.GROUP_EXECUTE);

		Files.createDirectories(basedir);
		return Files.createTempDirectory(basedir, tmpSuffix, PosixFilePermissions.asFileAttribute(permissions));
	}


	
	public static void checkAndDeleteDirectory(File d) throws IOException
	{
		if(d == null)
			return;
		if(!d.exists())
			return;
		Files.walkFileTree(d.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		d.delete();
	}
	
	
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
}