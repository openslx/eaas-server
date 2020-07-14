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
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.XmountOptions;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.api.metadata.DockerImport;
import de.bwl.bwfla.imagebuilder.api.metadata.ImageBuilderMetadata;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.json.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.bwl.bwfla.imagebuilder.api.ImageContentDescription.ArchiveFormat.DOCKER;


public abstract class MediumBuilder
{
	public abstract ImageHandle execute(Path workdir, ImageDescription description) throws BWFLAException;


	/* ==================== Internal Helpers ==================== */

	public static void delete(Path start, Logger log)
	{
		// Delete file or a directory recursively
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
		process.setCommand("sudo");
		process.addArgument("--non-interactive");
		process.addArguments("rm", "-r", "-f");
		process.addArgument(start.toString());
		process.setLogger(log);
		process.execute();
	}

	public static void unmount(Path path, Logger log)
	{
		try {
			EmulatorUtils.unmount(path, log);
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Unmounting '" + path.toString() + "' failed!\n", error);
		}
	}

	public static Path remount(Path device, Path mountpoint, XmountOptions options, Logger log)
			throws BWFLAException, IOException
	{
		MediumBuilder.sync(mountpoint, log);
		EmulatorUtils.unmount(mountpoint, log);
		return EmulatorUtils.xmount(device.toString(), mountpoint, options, log);
	}

	// we need to prepare the data source _before_ creating and mounting the dst image to support deduplication
	public static void prepare(List<ImageContentDescription> entries, Path workdir, Logger log) throws BWFLAException
	{
		for (ImageContentDescription entry : entries) {
			if(entry.getArchiveFormat() != null)
			switch (entry.getArchiveFormat()) {
				case DOCKER:
					ImageContentDescription.DockerDataSource ds = entry.getDockerDataSource();
					DockerTools docker = new DockerTools(workdir, ds, log);
					docker.pull();
					docker.unpack();
					break;
			}
		}
	}

	public static ImageBuilderMetadata build(List<ImageContentDescription> entries, Path dstdir, Path workdir, Logger log) throws IOException, BWFLAException {
		ImageBuilderMetadata md = null;
		for (ImageContentDescription entry : entries) {
			DataHandler handler;

			if (entry.getURL() != null) {
				handler = new DataHandler(new URLDataSource(entry.getURL()));
			} else {
				handler = entry.getData();
			}

			if (entry.getSubdir() != null){
				Path subdir = dstdir.resolve(entry.getSubdir());
				Files.createDirectories(subdir);
				dstdir = subdir;
			}

			switch (entry.getAction()) {
				case COPY:
					if (entry.getName() == null || entry.getName().isEmpty())
						throw new BWFLAException("entry with action COPY must have a valid name");

					Path outpath = dstdir.resolve(entry.getName());
					// FIXME: file names must be unique!
					if (outpath.toFile().exists())
						outpath = outpath.getParent().resolve(outpath.getFileName() + "-" + UUID.randomUUID());
					Files.copy(handler.getInputStream(), outpath);
					break;

				case EXTRACT:
					// Extract archive directly to destination!
					ImageHelper.extract(handler, dstdir, entry.getArchiveFormat(), workdir, log);
					break;

				case RSYNC:
					if(entry.getArchiveFormat().equals(DOCKER))
					{
						ImageContentDescription.DockerDataSource ds = entry.getDockerDataSource();
						if(ds.rootfs == null)
							throw new BWFLAException("Docker data source not ready. Prepare() before calling build");
						ImageHelper.rsync(ds.rootfs, dstdir, log);

						DockerImport dockerMd = new DockerImport();
						dockerMd.setImageRef(ds.imageRef);
						dockerMd.setLayers(Arrays.asList(ds.layers));
						dockerMd.setTag(ds.tag);
						dockerMd.setDigest(ds.digest);
						dockerMd.setEmulatorVersion(ds.version);
						dockerMd.setEmulatorType(ds.emulatorType);
						DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
						runner.setLogger(log);
						runner.setCommand("/bin/bash");
						dstdir.getParent().resolve("image");

						runner.addArgument("-c");

						Path imageDir = dstdir.getParent().resolve("image");

						if (Files.exists(imageDir)) {
							runner.addArgument("jq '{Cmd: .config.Cmd, Env: .config.Env, WorkingDir: .config.WorkingDir}' "
									+ dstdir + "/../image/blobs/\"$(jq -r .config.digest "
									+ dstdir + "/../image/blobs/\"$(jq -r .manifests[].digest "
									+ dstdir + "/../image/index.json | tr : /)\" | tr : /)\"");
							runner.start();
						} else {
							throw new BWFLAException("docker container doesn't contain image directory");
						}

						try {
							runner.waitUntilFinished();

							try (final JsonReader reader = Json.createReader(runner.getStdOutReader())) {
								final JsonObject json = reader.readObject();
								final ArrayList<String> envvars = new ArrayList<>();

								try {
									JsonArray envArray = json.getJsonArray("Env");
									for (int i = 0; i < envArray.size(); i++)
										envvars.add(envArray.getString(i));
								}
								catch(ClassCastException e)
								{
									log.warning("importing ENV failed");
									log.warning("Metadata object " + json.toString());
								}

								final ArrayList<String> cmds = new ArrayList<>();
								try {
									JsonArray cmdJson = json.getJsonArray("Cmd");
									for (int i = 0; i < cmdJson.size(); i++)
										cmds.add(cmdJson.getString(i));
								}
								catch (ClassCastException e)
								{
									log.warning("importing CMD failed");
									log.warning("Metadata object " + json.toString());
								}

								try {
									JsonString workDirObject = json.getJsonString("WorkingDir");
									if (workDirObject != null)
										dockerMd.setWorkingDir(workdir.toString());
								}
								catch(ClassCastException e)
								{
									log.warning("importing WorkingDir failed");
									log.warning("Metadata object " + json.toString());
								}
								dockerMd.setEntryProcesses(cmds);
								dockerMd.setEnvVariables(envvars);
							}

							md = dockerMd;
						}
						finally {
							runner.cleanup();
						}
					}

					break;
			}
		}
		return md;
	}

	public static void sync(Path path, Logger log)
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner("sync");
		process.setLogger(log);
		if (!process.execute())
			log.warning("Syncing filesystem for '" + path.toString() + "' failed!");
	}
}
