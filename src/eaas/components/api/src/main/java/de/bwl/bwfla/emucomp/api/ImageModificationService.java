package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.DiskDescription;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class ImageModificationService {

    public static void injectData(String imageLocation, List<ImageModificationRequest> requests, Logger log) throws BWFLAException
	{

		try (final ImageMounter mounter = new ImageMounter(log)) {
			final Path workdir = ImageMounter.createWorkingDirectory();
			mounter.addWorkingDirectory(workdir);

			// analyze mounted disk
			final DiskDescription disk = DiskDescription.read(Paths.get(imageLocation), log);
			if (!disk.hasPartitions())
				throw new BWFLAException("Disk seems to be not partitioned!");

			// Check each partition and each script...
			for (DiskDescription.Partition partition : disk.getPartitions()) {
				if (!partition.hasFileSystemType()) {
					log.info("Partition " + partition.getIndex() + " is unformatted, skip");
					continue;
				}
				ImageMounter.Mount mount = null;
				// Mount partition's filesystem and check...
				log.severe("XXXX mounting ....");
				mount = mounter.mount(Paths.get(imageLocation), workdir.resolve(imageLocation + ".part"), partition.getStartOffset(), partition.getSize());
				FileSystemType fstype = null;
				try {
					log.info("Got following type from partition:" + partition.getFileSystemType());
					fstype = FileSystemType.fromString(partition.getFileSystemType());
				} catch (Exception e) {
					continue;
				}
				final ImageMounter.Mount fsmnt = mounter.mount(mount, workdir.resolve("fs.fuse"), fstype);

				for (ImageModificationRequest request : requests) {
					// !_check(partition, condition) ||
					if (!_check(fsmnt.getMountPoint(), request.getCondition())) {
						log.severe("partition not valid");
						fsmnt.unmount(false);
						continue;  // ...not applicable, try next one
					}

					log.info("XXX Partition " + partition.getIndex() + " matches selectors! Applying patch...");
					switch (request.getAction()) {
						case COPY:
							copy(fsmnt, request, log);
							break;
						case EXTRACT_TAR:
							extractTar(workdir, fsmnt, request, log);
							break;
						default:
							throw new BWFLAException("requested action " + request.getAction() + "not implemented");
					}
				}
				mounter.unmount();
				log.info("Data inject was successful!");
			}
		}
		catch (IOException error) {
			throw new BWFLAException("Patching image failed!", error);
		}
	}

	private static void extractTar(Path workdir, ImageMounter.Mount fsmnt, ImageModificationRequest request, Logger log) throws BWFLAException {
		Path destination = Path.of(request.getDestination());
		if (destination.isAbsolute())
			destination = Path.of(destination.toString().substring(1));

		DeprecatedProcessRunner pr = new DeprecatedProcessRunner("curl");
		pr.addArguments("-L", "-o", workdir.toString() + "/out.tgz");
		pr.addArgument(request.getDataUrl());
		if(!pr.execute())
			throw new BWFLAException("failed to download " + request.getDataUrl());

		pr = new DeprecatedProcessRunner("sudo");
		pr.setWorkingDirectory(fsmnt.getMountPoint().resolve(destination));
		log.severe("working dir " + fsmnt.getMountPoint().resolve(destination));
		pr.addArguments("tar", "xvf", workdir.toString() + "/out.tgz");
		if(!pr.execute())
			throw new BWFLAException("failed to extract tar");
	}

	private static void copy(ImageMounter.Mount fsmnt, ImageModificationRequest request, Logger log) throws BWFLAException {
		Path destination = Path.of(request.getDestination());
		if (destination.isAbsolute())
			destination = Path.of(destination.toString().substring(1));

		log.severe(" XXX copy to " + destination);

		DeprecatedProcessRunner pr = new DeprecatedProcessRunner("sudo");
		log.severe("destination " + fsmnt.getMountPoint().resolve(destination));
		pr.addArguments("curl");
		pr.addArguments("-L", "-o", fsmnt.getMountPoint().resolve(destination).toString());
		pr.addArgument(request.getDataUrl());
		if(!pr.execute())
			throw new BWFLAException("failed to copy " + fsmnt.getMountPoint().resolve(destination).toString());
	}

	private static boolean _check(Path mountpoint, ImageModificationCondition condition)
	{
		if(condition == null)
			return true;

		final Predicate<Path> predicate = (subpath) -> {
			// Construct path relative to partition's mountpoint
			final Path target = mountpoint.resolve(subpath)
					.normalize();

			if (!target.startsWith(mountpoint))
				throw new IllegalArgumentException("Required subpath is invalid: " + subpath);

			return Files.exists(target);
		};

		final Function<Path, Path> mapper = (path) -> {
			if (path.isAbsolute())
				path = Path.of(path.toString().substring(1));

			return path;
		};

		return condition.getPaths().stream().map(Path::of).map(mapper).allMatch(predicate);
	}

	private static boolean _check(DiskDescription.Partition partition, ImageModificationCondition condition)
	{
		if(condition == null)
			return true;

		return Objects.equals(partition.getPartitionName(), condition.getPartitionName())
				&& Objects.equals(partition.getFileSystemType(), condition.getFstype());
	}
}
