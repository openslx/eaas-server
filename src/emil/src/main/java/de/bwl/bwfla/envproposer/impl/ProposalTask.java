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

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.api.imagebuilder.ImageBuilder;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.emil.ObjectClassification;
import de.bwl.bwfla.emil.datatypes.rest.ClassificationResult;
import de.bwl.bwfla.emil.tasks.ClassificationTask;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.MediumType;
import de.bwl.bwfla.emucomp.api.PartitionTableType;
import de.bwl.bwfla.envproposer.api.Proposal;
import de.bwl.bwfla.envproposer.api.ProposalRequest;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.client.ImageBuilderClient;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class ProposalTask extends BlockingTask<Object>
{
	private final String userid;
	private final ProposalRequest request;
	private final ImageBuilder imagebuilder;
	private final ObjectClassification classifier;
	private final BlobStore blobstore;
	private final String blobStoreAddress;
	private final Path workdir;

	private static final MediumType PREPARED_IMAGE_TYPE = MediumType.CDROM;


	public ProposalTask(ProposalRequest request, ObjectClassification classifier, String userid) throws BWFLAException
	{
		this.userid = userid;
		this.request = request;
		this.classifier = classifier;

		final Configuration config = ConfigurationProvider.getConfiguration();

		try {
			this.imagebuilder = ImageBuilderClient.get()
					.getImageBuilderPort(config.get("ws.imagebuilder"));

			this.blobstore = BlobStoreClient.get()
					.getBlobStorePort(config.get("ws.blobstore"));

			this.blobStoreAddress = config.get("rest.blobstore");
		}
		catch (Exception error) {
			throw new BWFLAException("Constructing web-services failed!", error);
		}

		this.workdir = ProposalTask.createWorkingDir(config.get("imagebuilder.basedir"));
	}

	@Override
	public Proposal execute() throws Exception
	{
		BlobHandle image = null;
		try {
			Path datadir = null;

			// Download remote archive and extract content
			final Path content = this.extract(request.getDataUrl(), request.getDataType());
			switch (request.getDataType()) {
				case BAGIT_ZIP:
				case BAGIT_TAR:
					// BagIt archives store content in a subdirectory
					datadir = this.findBagItContentRoot(content);
					break;

				default:
					datadir = content;
			}

			// Compute size and recompress content
			final long sizeInMb = 10 + this.computeSizeInMb(datadir);
			final Path archive = this.compress(datadir);
			ProposalTask.cleanup(content, log);

			// Build an image from data
			image = this.build(archive, sizeInMb);
			ProposalTask.cleanup(archive, log);

			// Propose environments
			return this.propose(image.toRestUrl(blobStoreAddress));
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Proposing environments failed!", error);
			if (image != null)
				blobstore.delete(image);

			throw error;
		}
		finally {
			log.info("Cleaning up...");
			ProposalTask.cleanup(workdir, log);
		}
	}


	// ========== Internal Helpers ====================

	private static Path createWorkingDir(String basedir) throws BWFLAException
	{
		try {
			return EaasFileUtils.createTempDirectory(Paths.get(basedir), "proposal-");
		}
		catch (Exception error) {
			throw new BWFLAException("Creating working directory failed!", error);
		}
	}

	private Path extract(String srcurl, ProposalRequest.DataType srctype) throws BWFLAException, IOException
	{
		log.info("Downloading and extracting data from: " + srcurl);

		final Path output = workdir.resolve("content");
		Files.createDirectories(output);

		final DataSource source = new URLDataSource(new URL(srcurl));
		try (final InputStream istream = source.getInputStream()) {
			switch (srctype) {
				case BAGIT_TAR:
				case TAR:
					// Stream and unpack directly from URL
					FileArchiveUtils.untar(istream, output, log);
					break;

				case BAGIT_ZIP:
				case ZIP:
					// unzip can't handle stdin streams, hence the archive needs to be downloaded first
					final Path archive = workdir.resolve("source.zip");
					Files.copy(istream, archive);
					FileArchiveUtils.unzip(archive, output, log);
					Files.deleteIfExists(archive);
					break;

				default:
					throw new BWFLAException("Not supported data-type: " + srctype.toString());
			}
		}

		return output;
	}

	private Path compress(Path content) throws BWFLAException
	{
		log.info("Re-compressing content as TAR archive...");

		final Path archive = workdir.resolve("data.tar.gz");
		FileArchiveUtils.tar(content, archive, log);
		return archive;
	}

	/** Compute directory's size in MB */
	private long computeSizeInMb(Path dir) throws IOException
	{
		log.info("Computing content size...");

		final ToLongFunction<Path> sizer = (path) -> {
			try {
				if (!Files.isDirectory(path))
					return Files.size(path);
			}
			catch (Exception error) {
				// Ignore it!
			}

			return 0L;
		};

		// Size in bytes
		final long size = Files.walk(dir)
				.mapToLong(sizer)
				.sum();

		return Math.max(1L, size / (1024L * 1024L));
	}

	private BlobHandle build(Path data, long sizeInMb) throws BWFLAException
	{
		log.info("Uploading content to blobstore...");

		final BlobDescription blobdesc = new BlobDescription()
				.setDescription("Input content for environment proposal " + this.getTaskId())
				.setNamespace("environment-proposer")
				.setDataFromFile(data)
				.setType(".tar");

		final BlobHandle blob = blobstore.put(blobdesc);

		try {
			log.info("Wrapping content in an image...");

			final FileSystemType fileSystemType = FileSystemType.ISO9660;

			final ImageDescription imgdesc = new ImageDescription()
					.setMediumType(PREPARED_IMAGE_TYPE)
					.setPartitionTableType(PartitionTableType.NONE)
					.setFileSystemType(fileSystemType)
					.setSizeInMb((int) sizeInMb);

			final ImageContentDescription entry = new ImageContentDescription()
					.setAction(ImageContentDescription.Action.EXTRACT)
					.setArchiveFormat(ImageContentDescription.ArchiveFormat.TAR)
					.setURL(new URL(blob.toRestUrl(blobStoreAddress)))
					.setName("data");

			imgdesc.addContentEntry(entry);

			// Build input image
			return ImageBuilderClient.build(imagebuilder, imgdesc, Duration.ofHours(1L))
					.getBlobHandle();
		}
		catch (MalformedURLException error) {
			throw new BWFLAException(error);
		}
		finally {
			log.info("Deleting content from blobstore...");
			blobstore.delete(blob);
		}
	}

	private Path findBagItContentRoot(Path basedir) throws BWFLAException, IOException
	{
		// Find marker file...
		final Optional<Path> result = Files.walk(basedir)
				.filter((path) -> path.getFileName().toString().equals("bagit.txt"))
				.findFirst();

		if (!result.isPresent())
			throw new BWFLAException("Invalid BagIt archive!");

		// Actual content is located in 'data' directory, relative to the marker file
		basedir = result.get().getParent();
		return basedir.resolve("data");
	}

	private FileCollection newFileCollection(String imageurl)
	{
		final FileCollectionEntry fce = new FileCollectionEntry();
		fce.setId(UUID.randomUUID().toString());
		fce.setType(Drive.DriveType.CDROM);
		fce.setUrl(imageurl);

		final FileCollection fc = new FileCollection();
		fc.id = UUID.randomUUID().toString();
		fc.files.add(fce);

		return fc;
	}

	private Proposal propose(String imageurl) throws Exception
	{
		log.info("Proposing environments...");

		final FileCollection fc = this.newFileCollection(imageurl);
		final ClassificationTask classification = classifier.newClassificationTask(fc, true, true, false, userid);
		classification.run();

		return new Proposal()
				.setImportedImageUrl(imageurl)
				.setImportedImageType(PREPARED_IMAGE_TYPE)
				.setResult((ClassificationResult) classification.getTaskResult().get());
	}

	private static void cleanup(Path workdir, Logger log)
	{
		// Delete path recursively
		try (final Stream<Path> stream = Files.walk(workdir)) {
			final Consumer<Path> deleter = (path) -> {
				try {
					Files.delete(path);
				}
				catch (Exception error) {
					final String message = "Deleting '" + path.toString() + "' failed! ("
							+ error.getClass().getName() + ": " + error.getMessage() + ")";

					log.warning(message);
				}
			};

			stream.sorted(Comparator.reverseOrder())
					.forEach(deleter);

			log.info("Path removed: " + workdir.toString());
		}
		catch (Exception error) {
			String message = "Deleting path failed!\n";
			log.log(Level.WARNING, message, error);
		}
	}
}
