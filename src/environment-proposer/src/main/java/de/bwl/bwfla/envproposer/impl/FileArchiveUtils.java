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

package de.bwl.bwfla.envproposer.impl;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class FileArchiveUtils
{
	public static void untar(InputStream source, Path outpath, Logger log) throws BWFLAException
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setCommand("tar");
		process.addArgument("--no-same-owner");
		process.addArguments("-xzf", "-");
		process.setWorkingDirectory(outpath);
		process.setLogger(log);
		if (!process.start())
			throw new BWFLAException("Starting untar failed!");

		try (OutputStream output = process.getStdInStream()) {
			IOUtils.copy(source, output);
			source.close();
			output.close();

			final int ret = process.waitUntilFinished();
			process.printStdOut();
			process.printStdErr();
			if (ret != 0)
				throw new BWFLAException("Running untar failed!");
		}
		catch (IOException error) {
			throw new BWFLAException("Extracting tar archive failed!", error);
		}
		finally {
			process.cleanup();
		}

		FileArchiveUtils.sync();
	}

	public static void unzip(Path source, Path outpath, Logger log) throws BWFLAException
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setCommand("unzip");
		process.addArgument("-qq");
		process.addArgument(source.toString());
		process.setWorkingDirectory(outpath);
		process.setLogger(log);
		if (!process.execute())
			throw new BWFLAException("Extracting zip archive failed!");

		FileArchiveUtils.sync();
	}

	public static void tar(Path content, Path archive, Logger log) throws BWFLAException
	{
		final DeprecatedProcessRunner tar = new DeprecatedProcessRunner("tar");
		tar.addArguments("--create", "--auto-compress", "--totals");
		tar.addArguments("--file", archive.toString());
		tar.addArguments("--directory", content.toString());
		try (Stream<Path> stream = Files.list(content)) {
			// Add each file to the archive...
			stream.forEach((path) -> {
				final Path file = path.getFileName();
				tar.addArgument(file.toString());
			});
		}
		catch (Exception error) {
			throw new BWFLAException(error);
		}

		tar.setWorkingDirectory(content);
		tar.setLogger(log);
		if (!tar.execute())
			throw new BWFLAException("Creating tar archive failed!");

		FileArchiveUtils.sync();
	}

	private static void sync() throws BWFLAException
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setCommand("sync");
		if (!process.execute())
			throw new BWFLAException("Syncing filesystem failed!");
	}
}
