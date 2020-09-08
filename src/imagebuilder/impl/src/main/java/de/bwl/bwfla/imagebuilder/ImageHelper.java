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

package de.bwl.bwfla.imagebuilder;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageHelper
{
	public static void extract(ImageContentDescription entry, Path dstPath, Path workdir, Logger log) throws BWFLAException
	{
		final ImageContentDescription.ArchiveFormat format = entry.getArchiveFormat();
		if (format == null)
			throw new BWFLAException("cannot extract. entry format not set.");

		final ImageContentDescription.StreamableDataSource source = entry.getStreamableDataSource();
		switch (format) {
			case ZIP:
				ImageHelper.unzip(source, dstPath, log);
				return;

			case TAR:
				ImageHelper.untar(source, dstPath, log);
				return;

			case SIMG:
				ImageHelper.extractSingularityImage(source, dstPath, workdir, log);
				return;

			default:
				throw new BWFLAException("Cannot extract entry! Unsupported archive format: " + format.toString());
		}
	}

	private static void extractSingularityImage(ImageContentDescription.StreamableDataSource source, Path dstdir, Path workdir, Logger log)
			throws BWFLAException
	{
		final Path image = workdir.resolve("image-" + UUID.randomUUID().toString() + ".simg");
		try (InputStream istream = source.openInputStream()) {
			Files.copy(istream, image);

			final String command = "sudo --non-interactive /usr/local/bin/singularity image.export " + image.toString()
					+ " | tar -C " + dstdir.toString() + " -v -xf -";

			final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
			process.setCommand("/bin/bash");
			process.addArguments("-c");
			process.addArgument(command);
			process.setLogger(log);
			if (!process.execute())
				throw new IOException("Running image export failed!");
		}
		catch (Exception error) {
			throw new BWFLAException("Extracting singularity image failed!", error);
		}
		finally {
			try {
				Files.deleteIfExists(image);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Deleting '" + image.toString() + "' failed!", error);
			}
		}
	}

	private static void untar(ImageContentDescription.StreamableDataSource source, Path dstdir, Logger log)
			throws BWFLAException
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setCommand("sudo");
		process.addArguments("--non-interactive");
		process.addArgument("tar");
		process.addArgument("--no-same-owner");
		process.addArguments("-xzf", "-");
		process.setWorkingDirectory(dstdir);
		process.setLogger(log);
		if (!process.start())
			throw new BWFLAException("Starting untar failed!");

		try (InputStream in = source.openInputStream(); OutputStream out = process.getStdInStream()) {
			IOUtils.copy(in, out);
			in.close();
			out.close();

			int ret = process.waitUntilFinished();
			process.printStdOut();
			process.printStdErr();
			if (ret != 0)
				throw new BWFLAException("Running untar failed!");
		}
		catch (IOException error) {
			throw new BWFLAException("Extracting tar archive failed!", error);
		}
		finally {
			if (process.isProcessRunning())
				process.kill();

			process.cleanup();
		}
	}


	private static void unzip(ImageContentDescription.StreamableDataSource source, Path dstdir, Logger log)
			throws BWFLAException
	{
		try (final InputStream input = source.openInputStream()) {
			log.info("Extracting zip archive into image...");
			Zip32Utils.unzip(input, dstdir.toFile());
		}
		catch (Exception error)	{
			throw new BWFLAException("Extracting zip archive failed!", error);
		}
	}

	public static void rsync(Path sourceDir, Path targetDir, Logger log) throws BWFLAException {
		DeprecatedProcessRunner rsyncRunner = new DeprecatedProcessRunner();
		rsyncRunner.setCommand("rsync");
		rsyncRunner.addArgument("-crlptgoD");
		rsyncRunner.addArguments( "--delete"); // , "--no-o", "--no-g");
		rsyncRunner.addArgument(sourceDir.toAbsolutePath().toString() + "/");
		rsyncRunner.addArgument(targetDir.toAbsolutePath().toString());
		rsyncRunner.setLogger(log);
		if (!rsyncRunner.execute())
			throw new BWFLAException("rsync failed");
	}
}
