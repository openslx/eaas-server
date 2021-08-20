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

import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.api.metadata.DockerImport;
import de.bwl.bwfla.imagebuilder.api.metadata.ImageBuilderMetadata;

import javax.json.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import static de.bwl.bwfla.imagebuilder.api.ImageContentDescription.ArchiveFormat.DOCKER;


public abstract class MediumBuilder
{
	public abstract ImageHandle execute(Path workdir, ImageDescription description) throws BWFLAException;


	/* ==================== Internal Helpers ==================== */

	private static void executeSiegfried(File src, Logger LOG) throws IOException, InterruptedException, BWFLAException {
		var srcName = src.getAbsolutePath();

		LOG.severe("--------------Starting siegfried! " + srcName);
		File siegfriedFileName = new File("/tmp/siegfried_input.json");

		ProcessBuilder processBuilderUpdate = new ProcessBuilder("sf", "-update");
		var update = processBuilderUpdate.start();
		update.waitFor();

		ArrayList<String> arguments = new ArrayList<String>();
		arguments.add("sf");
		arguments.add("-json");
		arguments.add(srcName);

		File logfile = new File("/tmp/sf_input_log.log");
		ProcessBuilder processBuilder = new ProcessBuilder(arguments).redirectError(logfile).redirectOutput(siegfriedFileName);

		Process process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}


		if(process!=null){
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (process.exitValue() == 0) {
				LOG.info("Successfully executed Siegfried for input files.");

//				uploadSiegfriedData(siegfriedFileName);
				//TODO put url/ file into output?
			} else {
				LOG.severe("Something went wrong while executing Siegfried!");
//				uploadSiegfriedData(siegfriedFileName);

			}
		}
	}

	public static ImageBuilderMetadata build(List<ImageContentDescription> entries, Path dstdir, Path workdir, Logger log) throws IOException, BWFLAException {
		ImageBuilderMetadata md = null;
		for (ImageContentDescription entry : entries) {
			Path subdir = dstdir;
			if (entry.getSubdir() != null){
				subdir = dstdir.resolve(entry.getSubdir());
				log.severe("creating " + subdir.toString());
				Files.createDirectories(subdir);
			}

			log.severe(" ----------------------------------------------------################################ ENTRY NAME: " + entry.getName());
			log.severe("DEST DIR:" + dstdir.toString());
			log.severe("WORK DIR:" + workdir.toString());

			switch (entry.getAction()) {
				case COPY:
					if (entry.getName() == null || entry.getName().isEmpty())
						throw new BWFLAException("entry with action COPY must have a valid name");

					Path outpath = subdir.resolve(entry.getName());
					// FIXME: file names must be unique!
					if (outpath.toFile().exists())
						outpath = outpath.getParent().resolve(outpath.getFileName() + "-" + UUID.randomUUID());

					final ImageContentDescription.StreamableDataSource source = entry.getStreamableDataSource();
					log.severe("copy " + outpath);
					try (InputStream istream = source.openInputStream()) {
						Files.copy(istream, outpath);
					}

					break;

				case EXTRACT:
					// Extract archive directly to destination!
					ImageHelper.extract(entry, subdir, workdir, log);
					break;

				case RSYNC:
					if(entry.getArchiveFormat().equals(DOCKER))
					{
						ImageContentDescription.DockerDataSource ds = entry.getDockerDataSource();
						DockerTools docker = new DockerTools(workdir, ds, log);
						docker.pull(subdir);

						DockerImport dockerMd = new DockerImport();
						dockerMd.setImageRef(ds.imageRef);
						// dockerMd.setLayers(Arrays.asList(ds.layers));
						dockerMd.setTag(ds.tag);
						dockerMd.setDigest(ds.digest);
						dockerMd.setEmulatorVersion(ds.version);
						dockerMd.setEmulatorType(ds.emulatorType);
						dockerMd.setWorkingDir(ds.workingDir);
						dockerMd.setEntryProcesses(ds.entryProcesses);
						dockerMd.setEnvVariables(ds.envVariables);

						md = dockerMd;

					}
					break;
			}
		}

		try {
			executeSiegfried(dstdir.toFile(), log);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return md;
	}
}
