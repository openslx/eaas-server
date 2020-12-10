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

package de.bwl.bwfla.imagearchive.generalization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.DiskDescription;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.ImageMounter;
import de.bwl.bwfla.emucomp.api.QcowOptions;
import org.apache.tamaya.ConfigurationProvider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ImageGeneralizationPatch {
	private Path location;
	private String name;
	private String description;
	private List<Script> scripts;
	private String blobStoreAddressSoap;
	private String blobStoreRestAddress;

	ImageGeneralizationPatch() {
		blobStoreAddressSoap = ConfigurationProvider.getConfiguration().get("emucomp.blobstore_soap");
		blobStoreRestAddress = ConfigurationProvider.getConfiguration().get("rest.blobstore");
	}

	private static Path prepareCow(Path dir, String backingFile) throws BWFLAException {
		String filename = UUID.randomUUID().toString();
		QcowOptions options = new QcowOptions();
		options.setBackingFile(backingFile);

		Path destImgFile = dir.resolve(filename);
		EmulatorUtils.createCowFile(destImgFile, options);
		return destImgFile;
	}

	private URL publishImage(Path image) throws MalformedURLException, BWFLAException {
		BlobHandle handle = null;

		final BlobDescription blob = new BlobDescription()
				.setDescription("Generalized QCOW")
				.setNamespace("emulator-snapshots")
				.setDataFromFile(image)
				.setType(".qcow")
				.setName(image.getFileName().toString());
		handle = BlobStoreClient.get().getBlobStorePort(blobStoreAddressSoap).put(blob);
		return new URL(handle.toRestUrl(blobStoreRestAddress));
	}

	public URL applyto(String backingFile, Logger log) throws BWFLAException
	{
		log.info("Patching image: " + backingFile);
		try (final ImageMounter mounter = new ImageMounter(log)) {
			final Path workdir = ImageMounter.createWorkingDirectory();
			mounter.addWorkingDirectory(workdir);

			Path image = prepareCow(workdir, backingFile);

			// Mount image and try to find available partitions...
			ImageMounter.Mount rawmnt = mounter.mount(image, workdir.resolve(image.getFileName() + ".fuse"));
			final DiskDescription disk = DiskDescription.read(rawmnt.getTargetImage(), log);
			if (!disk.hasPartitions())
				throw new BWFLAException("Disk seems to be not partitioned!");

			log.info("Searching partition to be patched...");

			// Check each partition and each script...
			for (DiskDescription.Partition partition : disk.getPartitions()) {
				if (!partition.hasFileSystemType()) {
					log.info("Partition " + partition.getIndex() + " is unformatted, skip");
					continue;
				}

				// Mount partition's filesystem and check...
				rawmnt = mounter.remount(rawmnt, partition.getStartOffset(), partition.getSize());
				final FileSystemType fstype = FileSystemType.fromString(partition.getFileSystemType());
				final ImageMounter.Mount fsmnt = mounter.mount(rawmnt, workdir.resolve("fs.fuse"), fstype);
				for (Script script : scripts) {
					if (!script.check(partition) || !script.check(fsmnt.getMountPoint()))
						continue;  // ...not applicable, try next one

					log.info("Partition " + partition.getIndex() + " matches selectors! Applying patch...");
					if (!script.apply(fsmnt.getMountPoint(), log))
						throw new BWFLAException("Applying patch failed!");

					log.info("Patching was successful!");
					return publishImage(image);
				}

				log.info("Partition " + partition.getIndex() + " does not match selectors, skip");

				fsmnt.unmount(false);
			}

			throw new BWFLAException("Patching image failed! No matching partition was found!");
		}
		catch (IOException error) {
			throw new BWFLAException("Patching image failed!", error);
		}
	}

	private ImageGeneralizationPatch setName(String name)
	{
		this.name = name;
		return this;
	}

	@JsonProperty("name")
	public String getName()
	{
		return name;
	}

	private ImageGeneralizationPatch setDescription(String desc)
	{
		this.description = desc;
		return this;
	}

	@JsonProperty("description")
	public String getDescription()
	{
		return description;
	}

	private ImageGeneralizationPatch setScripts(List<Script> scripts)
	{
		this.scripts = Collections.unmodifiableList(scripts);
		return this;
	}

	@JsonProperty("scripts")
	private List<Script> getScripts()
	{
		return scripts;
	}

	ImageGeneralizationPatch setLocationDir(Path path)
	{
		// Update locations of all scripts too...
		scripts.forEach((script) -> script.setLocationDir(path));

		this.location = path;
		return this;
	}

	@JsonIgnore
	Path getLocationDir()
	{
		return location;
	}


	// ===== Internal Helpers =========================

	private static class Script
	{
		private Condition condition;
		private Path location;

		public boolean check(DiskDescription.Partition partition)
		{
			return Objects.equals(partition.getPartitionName(), condition.getPartitionName())
					&& Objects.equals(partition.getFileSystemType(), condition.getFileSystemType());
		}

		public boolean check(Path mountpoint)
		{
			final Predicate<Path> predicate = (subpath) -> {
				// Construct path relative to partition's mountpoint
				final Path target = mountpoint.resolve(subpath)
						.normalize();

				if (!target.startsWith(mountpoint))
					throw new IllegalArgumentException("Required subpath is invalid: " + subpath);

				return Files.exists(target);
			};

			return condition.getRequiredPaths()
					.stream()
					.allMatch(predicate);
		}

		public boolean apply(Path mountpoint, Logger log)
		{
			// Run locally installed script
			final DeprecatedProcessRunner patcher = new DeprecatedProcessRunner()
					.setCommand(location.toString())
					.addArgument(mountpoint.toString())
					.setLogger(log);

			return patcher.execute();
		}

		public Script setCondition(Condition condition)
		{
			this.condition = condition;
			return this;
		}

		@JsonProperty("condition")
		public Condition getCondition()
		{
			return condition;
		}

		public Script setPath(Path path)
		{
			this.location = path;
			return this;
		}

		@JsonSetter
		public Script setPath(String path)
		{
			return this.setPath(Path.of(path));
		}

		@JsonProperty("path")
		public Path getPath()
		{
			return location;
		}

		@JsonIgnore
		private Script setLocationDir(Path dir)
		{
			if (location.isAbsolute())
				throw new IllegalArgumentException("Script's location is already absolute!");

			this.location = dir.resolve(location)
					.normalize();

			return this;
		}
	}

	private static class Condition
	{
		private String partname;
		private String fstype;
		private List<Path> paths;

		public Condition setPartitionName(String name)
		{
			this.partname = name;
			return this;
		}

		@JsonProperty("partition_name")
		public String getPartitionName()
		{
			return partname;
		}

		public Condition setFileSystemType(String type)
		{
			this.fstype = type;
			return this;
		}

		@JsonProperty("filesystem")
		public String getFileSystemType()
		{
			return fstype;
		}

		public Condition setRequiredPaths(List<Path> paths)
		{
			// Convert all absolute paths to relative
			final Function<Path, Path> mapper = (path) -> {
				if (path.isAbsolute())
					path = Path.of(path.toString().substring(1));

				return path;
			};

			this.paths = paths.stream()
					.map(mapper)
					.collect(Collectors.toList());

			return this;
		}

		@JsonSetter
		public Condition setRequiredPaths(Collection<String> paths)
		{
			final List<Path> list = paths.stream()
					.map(Path::of)
					.collect(Collectors.toList());

			return this.setRequiredPaths(list);
		}

		@JsonProperty("required_paths")
		public List<Path> getRequiredPaths()
		{
			return paths;
		}
	}
}
