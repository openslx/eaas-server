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

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.emucomp.api.ImageMounter;
import de.bwl.bwfla.imagebuilder.api.IImageBuilder;
import de.bwl.bwfla.imagebuilder.api.ImageBuildHandle;
import de.bwl.bwfla.imagebuilder.api.ImageBuilderResult;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.emucomp.api.MediumType;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@Singleton
public class ImageBuilderBackend implements IImageBuilder
{
	private final Logger log = Logger.getLogger(ImageBuilderBackend.class.getName());

	@Inject
	@Config("imagebuilder.basedir")
	private String basedir = null;

	private final Map<MediumType, MediumBuilder> builders;
	private final BlobStore blobstore;
	private final TaskManager builds;


	public ImageBuilderBackend()
	{
		this.builders = new HashMap<MediumType, MediumBuilder>();

		final Configuration config = ConfigurationProvider.getConfiguration();
		try {
			final String blobStoreAddress = config.get("imagebuilder.blobstore");
			this.blobstore = BlobStoreClient.get()
					.getBlobStorePort(blobStoreAddress);
		}
		catch (Exception error) {
			throw new IllegalStateException("Creating BlobStore client failed!", error);
		}

		try {
			this.builds = new TaskManager();
		}
		catch (Exception error) {
			throw new IllegalStateException("Creating TaskManager failed!", error);
		}

		// Register all supported builders
		builders.put(MediumType.HDD, new MediumBuilderHDD());
		builders.put(MediumType.CDROM, new MediumBuilderCD());
	}


	/* =============== IImageBuilder Implementation =============== */

	@Override
	public ImageBuildHandle build(ImageDescription description) throws BWFLAException
	{
		final Path workdir = this.createWorkingDir();
		final String tid = builds.submit(new ImageBuildTask(workdir, description));
		return new ImageBuildHandle(tid);
	}

	@Override
	public boolean isDone(ImageBuildHandle build) throws BWFLAException
	{
		final TaskInfo<ImageBuilderResult> info = builds.lookup(build.getId());
		if (info == null)
			throw new BWFLAException("Invalid image-build handle!");

		return info.result().isDone();
	}

	@Override
	public ImageBuilderResult get(ImageBuildHandle build) throws BWFLAException
	{
		final TaskInfo<ImageBuilderResult> info = builds.lookup(build.getId());
		if (info == null)
			throw new BWFLAException("Invalid image-build handle!");

		try {
			return info.result().get();
		}
		catch (Exception error) {
			throw new BWFLAException("Retrieving image-build result failed!", error);
		}
		finally {
			builds.remove(build.getId());
		}
	}


	/* =============== Internal Helpers =============== */

	private Path createWorkingDir() throws BWFLAException
	{
		try {
			return EaasFileUtils.createTempDirectory(Paths.get(basedir), "build-");
		}
		catch (Exception error) {
			final String message = "Creating working directory failed!";
			log.log(Level.WARNING, message, error);
			throw new BWFLAException(message, error);
		}
	}

	private ImageHandle build(Path workdir, ImageDescription description) throws BWFLAException
	{
		try {
			final MediumBuilder builder = builders.get(description.getMediumType());
			if (builder == null) {
				final String message = "Requested medium type '" + description.getMediumType() + "' not supported!";
				throw new BWFLAException(message);
			}

			return builder.execute(workdir, description);
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Building image failed!", error);
			throw error;
		}
	}

	private static class TaskManager extends de.bwl.bwfla.common.taskmanager.TaskManager<ImageBuilderResult>
	{
		public TaskManager() throws NamingException
		{
			super("IMAGE-BUILDER-TASKS", InitialContext.doLookup("java:jboss/ee/concurrency/executor/batch"));
		}
	}

	private class ImageBuildTask extends BlockingTask<ImageBuilderResult>
	{
		private final ImageBuilderBackend backend;
		private final Path workdir;
		private final ImageDescription description;

		public ImageBuildTask(Path workdir, ImageDescription description)
		{
			this.backend = ImageBuilderBackend.this;
			this.workdir = workdir;
			this.description = description;
		}

		@Override
		protected ImageBuilderResult execute() throws Exception
		{
			try {
				final ImageHandle image = backend.build(workdir, description);

				// Upload image to the BlobStore
				final BlobDescription blob = new BlobDescription()
						.setDescription("ImageBuilder image")
						.setNamespace("imagebuilder-outputs")
						.setDataFromFile(image.getPath())
						.setName(image.getName())
						.setType(image.getType());

				ImageBuilderResult result = new ImageBuilderResult();
				result.setBlobHandle(blobstore.put(blob));
				result.setMetadata(image.getMetadata());
				return result;
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Building new image failed!", error);
				throw error;
			}
			finally {
				ImageMounter.delete(workdir, log);
			}
		}
	}
}
