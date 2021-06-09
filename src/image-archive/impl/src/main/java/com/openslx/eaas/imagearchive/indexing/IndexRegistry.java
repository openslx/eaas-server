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

package com.openslx.eaas.imagearchive.indexing;

import com.openslx.eaas.imagearchive.AbstractRegistry;
import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.BlobKind;
import com.openslx.eaas.imagearchive.indexing.impl.CheckpointIndex;
import com.openslx.eaas.imagearchive.indexing.impl.ContainerIndex;
import com.openslx.eaas.imagearchive.indexing.impl.ImageIndex;
import com.openslx.eaas.imagearchive.indexing.impl.ImportIndex;
import com.openslx.eaas.imagearchive.indexing.impl.MachineIndex;
import com.openslx.eaas.imagearchive.indexing.impl.MetaDataIndex;
import com.openslx.eaas.imagearchive.indexing.impl.RomIndex;
import com.openslx.eaas.imagearchive.indexing.impl.TemplateIndex;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Stream;


public class IndexRegistry extends AbstractRegistry<BlobIndex<?>>
{
	public Stream<BlobKind> list()
	{
		return entries.stream()
				.filter(Objects::nonNull)
				.map(BlobIndex::kind);
	}

	public IndexRegistry rebuild(StorageRegistry storage) throws BWFLAException
	{
		int numFailures = 0;

		// TODO: build indexes in parallel!

		final var logger = ArchiveBackend.logger();
		logger.info("Building all data-indexes...");
		for (var index : super.entries) {
			if (index == null)
				continue;

			try {
				index.rebuild(storage);
			}
			catch (Exception error) {
				logger.log(Level.WARNING, "Building index '" + index.name() + "' failed!", error);
				++numFailures;
			}
		}

		if (numFailures > 0)
			throw new BWFLAException("Building data-indexes failed!");

		logger.info("All data-indexes built successfully");
		return this;
	}

	public MetaDataIndex environments()
	{
		return this.lookup(BlobKind.ENVIRONMENT, MetaDataIndex.class);
	}

	public MetaDataIndex sessions()
	{
		return this.lookup(BlobKind.SESSION, MetaDataIndex.class);
	}

	public ContainerIndex containers()
	{
		return this.lookup(BlobKind.CONTAINER, ContainerIndex.class);
	}

	public MachineIndex machines()
	{
		return this.lookup(BlobKind.MACHINE, MachineIndex.class);
	}

	public TemplateIndex templates()
	{
		return this.lookup(BlobKind.TEMPLATE, TemplateIndex.class);
	}

	public CheckpointIndex checkpoints()
	{
		return this.lookup(BlobKind.CHECKPOINT, CheckpointIndex.class);
	}

	public ImageIndex images()
	{
		return this.lookup(BlobKind.IMAGE, ImageIndex.class);
	}

	public RomIndex roms()
	{
		return this.lookup(BlobKind.ROM, RomIndex.class);
	}

	public ImportIndex imports()
	{
		return imports;
	}

	public static IndexRegistry create() throws BWFLAException
	{
		final var registry = new IndexRegistry();
		registry.insert(new MetaDataIndex(BlobKind.ENVIRONMENT));
		registry.insert(new MetaDataIndex(BlobKind.SESSION));
		registry.insert(new ContainerIndex());
		registry.insert(new MachineIndex());
		registry.insert(new TemplateIndex());
		registry.insert(new CheckpointIndex());
		registry.insert(new ImageIndex());
		registry.insert(new RomIndex());
		registry.insert(new ImportIndex());
		return registry;
	}


	// ===== Internal Helpers ==============================

	private ImportIndex imports;

	private IndexRegistry()
	{
		super();
	}

	private void insert(BlobIndex<?> index)
	{
		super.insert(index.kind(), index);
	}

	private void insert(ImportIndex index)
	{
		this.imports = index;
	}
}
