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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class Iso9660Utils {
	private final static String newline = System.getProperty("line.separator");
	private final static String MKISOFS_BIN = "mkisofs";

	private Iso9660Utils() {
	}

	public static void mountIso9660(Path isoFile, Path mountpoint)
			throws IOException, FileNotFoundException {
		if (isoFile == null) {
			throw new NullPointerException("isoFile == null");
		}
		if (mountpoint == null) {
			throw new NullPointerException("mountpoint == null");
		}
		if (!(Files.exists(isoFile) && Files.isReadable(isoFile))) {
			throw new FileNotFoundException(isoFile.toString());
		}
		if (!(Files.exists(mountpoint) && Files.isDirectory(mountpoint))) {
			throw new FileNotFoundException(mountpoint.toString());
		}

		DeprecatedProcessRunner proc = new DeprecatedProcessRunner("fuseiso");
		proc.addArguments(isoFile.toString(), mountpoint.toString());
		if (!proc.execute()) {
			throw new IOException("Could not mount file.");
		}
	}

	public static void unmountIso9660(Path mountpoint) throws IOException,
			FileNotFoundException {
		if (mountpoint == null) {
			throw new NullPointerException("mountpoint == null");
		}
		if (!(Files.exists(mountpoint) && Files.isDirectory(mountpoint))) {
			throw new FileNotFoundException(mountpoint.toString());
		}

		DeprecatedProcessRunner proc = new DeprecatedProcessRunner("fusermount");
		proc.addArguments("-u", mountpoint.toString());
		if (!proc.execute()) {
			throw new IOException("Could not unmount file.");
		}

	}

	public static void createIso9660(Path isoFile, List<Path> content)
			throws IOException {
		if (isoFile == null) {
			throw new NullPointerException("isoFile == null");
		}
		if (content == null) {
			throw new NullPointerException("content == null");
		}

		StringBuilder paths = new StringBuilder(content.size() * 30);
		for (Path path : content) {
			if (!Files.exists(path)) {
				throw new FileNotFoundException(path.toString());
			}
			paths.append(path.toString()).append(newline);
		}
		
		DeprecatedProcessRunner proc = new DeprecatedProcessRunner(MKISOFS_BIN);
		proc.addArguments("-o", isoFile.toString(),
				          "-graft-points",
				          "-path-list", "-",
				          "-iso-level", "3",
				          "-J",
				          "-R",
				          "-hfs");
		if (!proc.start()) {
			throw new IOException("Failed to start mkisofs process.");
		}
		try {
			Writer writer = proc.getStdInWriter();
			writer.write(paths.toString());
			writer.close();
		} catch (IOException e) {
			proc.stop();
			proc.cleanup();
			throw new IOException("Cannot feed file list to mkisofs. Aborting ISO9660 file creation.", e);
		}
		proc.waitUntilFinished();
		
		proc.printStdOut();
		proc.printStdErr();
		
		proc.cleanup();
	}
}
