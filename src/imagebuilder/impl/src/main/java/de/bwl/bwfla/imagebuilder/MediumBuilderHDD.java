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


import de.bwl.bwfla.api.imagearchive.ImageMetadata;
import de.bwl.bwfla.api.imagearchive.ImageNameIndex;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imagebuilder.api.ImageContentDescription;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;
import de.bwl.bwfla.imagebuilder.api.metadata.ImageBuilderMetadata;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;


public class MediumBuilderHDD extends MediumBuilder
{
	private final Map<FileSystemType, IFileSystemMaker> fsMakers;
	private final Map<PartitionTableType, IPartitionTableMaker> ptMakers;


	public MediumBuilderHDD()
	{
		this.fsMakers = new HashMap<FileSystemType, IFileSystemMaker>();
		this.ptMakers = new HashMap<PartitionTableType, IPartitionTableMaker>();


		// Register makers for FAT variants
		{
			final FileSystemType[] fstypes = new FileSystemType[] {
					FileSystemType.VFAT,
					FileSystemType.FAT16,
					FileSystemType.FAT32
			};

			for (FileSystemType fstype : fstypes)
				fsMakers.put(fstype, new FileSystemMakerFAT(fstype));
		}

		// Register maker for NTFS
		fsMakers.put(FileSystemType.NTFS, new FileSystemMakerNTFS());

		// Register makers for EXT variants
		{
			final FileSystemType[] fstypes = new FileSystemType[] {
					FileSystemType.EXT2,
					FileSystemType.EXT3,
					FileSystemType.EXT4
			};

			for (FileSystemType fstype : fstypes)
				fsMakers.put(fstype, new FileSystemMakerEXT(fstype));
		}

		// Register maker for MBR
		ptMakers.put(PartitionTableType.MBR, new PartitionTableMakerMBR());
	}


	@Override
	public ImageHandle execute(Path workdir, ImageDescription description) throws BWFLAException
	{
		final String outname = "image";
		final String outtype = ".qcow2";

		final Path qcow = workdir.resolve(outname + outtype);

		final PrefixLogger log = new PrefixLogger(this.getClass().getSimpleName());
		log.getContext().add(workdir.getFileName().toString());
		{
			final String message = "Building image " + description.toShortSummary()
					+ " of size " + description.getSizeInMb() + "MB...";

			log.info(message);
		}

		try (final ImageMounter mounter = new ImageMounter(log)) {
			MediumBuilderHDD.prepare(description.getContentEntries(), workdir, log);

			// Create base qcow container
			QcowOptions options = new QcowOptions();
			options.setSize(description.getSizeInMb() + "M");
			String backingFile = lookupBackingFile(description);
			if(backingFile != null)
				options.setBackingFile(backingFile);
			EmulatorUtils.createCowFile(qcow, options, log);

			if(description.getFileSystemType() == FileSystemType.RAW)
				return new ImageHandle(qcow, outname, outtype);

			// Mount it as raw disk-image
			ImageMounter.Mount rawmnt = mounter.mount(qcow, workdir.resolve(qcow.getFileName() + ".fuse"));

			// Partition the raw disk-image
			if (backingFile == null && description.getPartitionTableType() != PartitionTableType.NONE) {
				final int offset = description.getPartitionOffset();
				final String fstype = description.getFileSystemType().name();
				this.partition(description.getPartitionTableType(), rawmnt.getTargetImage(), fstype, offset, log);

				// Re-mount partition only
				rawmnt = rawmnt.remount(offset);
			}

			// Prepare filesystem on the partition
			final FileSystemType fstype = description.getFileSystemType();
			if (backingFile == null)
				this.makefs(fstype, rawmnt.getTargetImage(), description.getLabel(), log);

			rawmnt.sync();

			// Finally, build image's content
			final ImageMounter.Mount fsmnt = mounter.mount(rawmnt, workdir.resolve("fs"), fstype);
			final ImageBuilderMetadata md = MediumBuilderHDD.build(description.getContentEntries(), fsmnt.getMountPoint(), workdir, log);

			// Unmount everything and flush data to disk!
			mounter.unmount();

			log.info("Image built successfully");

			// The final image!
			ImageHandle result = new ImageHandle(qcow, outname, outtype);
			result.setMetadata(md);
			return result;
		}
		catch (Exception error) {
			if (error instanceof BWFLAException)
				throw (BWFLAException) error;

			throw new BWFLAException(error);
		}
	}

	private String lookupBackingFile(ImageDescription description) {
		String backingFile = null;
		int layerIndex = Integer.MAX_VALUE;

		for(ImageContentDescription e : description.getContentEntries())
		{
			if(e.getArchiveFormat() == null || !e.getArchiveFormat().equals(ImageContentDescription.ArchiveFormat.DOCKER))
				continue;

			if(!(e.getDataSource() instanceof ImageContentDescription.DockerDataSource))
				continue;

			ImageContentDescription.DockerDataSource ds = e.getDockerDataSource();
			if(ds.imageArchiveHost == null || ds.layers == null)
				continue;

			EnvironmentsAdapter envHelper = new EnvironmentsAdapter(ds.imageArchiveHost);
			try {
				ImageNameIndex index = envHelper.getNameIndexes("emulators");
				if(index.getEntries() == null)
					continue;

				for(ImageNameIndex.Entries.Entry _entry : index.getEntries().getEntry())
				{
					ImageMetadata indexEntry = _entry.getValue();
					if(indexEntry.getProvenance() != null && indexEntry.getProvenance().getLayers() != null) {
						List layerList = indexEntry.getProvenance().getLayers();
						for(String l : ds.layers) {
							int idx = layerList.indexOf(l);
							if (idx >= 0)
							{
								// TODO: make the url creation more elegant 
								if(idx < layerIndex)
									backingFile = ds.imageArchiveHost + "/imagearchive/emulators/" + indexEntry.getImage().getId();
							}
						}
					}
				}
			} catch (BWFLAException e1) {
				e1.printStackTrace();
				return null;
			}
		}
		// log.info("found lookupBackingFile: " + backingFile);
		return backingFile;
	}


	/* ==================== Internal Helpers ==================== */

	private void partition(PartitionTableType pttype, Path device,String fsType, int partStartOffset, Logger log) throws BWFLAException
	{
		final IPartitionTableMaker ptmaker = ptMakers.get(pttype);
		if (ptmaker == null) {
			final String message = "Requested partition-table type '" + pttype + "' not supported!";
			throw new BWFLAException(message);
		}

		ptmaker.execute(device, partStartOffset, fsType, log);
	}

	private void makefs(FileSystemType fstype, Path device, String label, Logger log) throws BWFLAException
	{
		final IFileSystemMaker fsmaker = fsMakers.get(fstype);
		if (fsmaker == null) {
			final String message = "Requested filesystem type '" + fstype + "' not supported!";
			throw new BWFLAException(message);
		}

		fsmaker.execute(device, label, log);
	}
}
