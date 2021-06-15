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
						DockerTools docker = new DockerTools(workdir, ds, log);
						docker.pull(dstdir);

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
		return md;
	}
}
