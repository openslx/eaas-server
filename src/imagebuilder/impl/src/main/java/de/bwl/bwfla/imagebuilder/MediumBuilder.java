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
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.api.metadata.DockerImport;
import de.bwl.bwfla.imagebuilder.api.metadata.ImageBuilderMetadata;

import javax.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import static de.bwl.bwfla.imagebuilder.api.ImageContentDescription.ArchiveFormat.DOCKER;


public abstract class MediumBuilder
{
	public abstract ImageHandle execute(Path workdir, ImageDescription description) throws BWFLAException;


	/* ==================== Internal Helpers ==================== */

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

					final ImageContentDescription.StreamableDataSource source = entry.getStreamableDataSource();
					try (InputStream istream = source.openInputStream()) {
						Files.copy(istream, outpath);
					}

					break;

				case EXTRACT:
					// Extract archive directly to destination!
					ImageHelper.extract(entry, dstdir, workdir, log);
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
										dockerMd.setWorkingDir(workDirObject.toString());
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
}
