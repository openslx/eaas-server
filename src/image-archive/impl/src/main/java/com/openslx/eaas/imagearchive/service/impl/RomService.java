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

package com.openslx.eaas.imagearchive.service.impl;

import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.indexing.impl.RomIndex;
import com.openslx.eaas.imagearchive.service.BlobService;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;


public class RomService extends BlobService<RomIndex.Record>
{
	public static RomService create(ArchiveBackend backend)
	{
		final var index = backend.indexes()
				.roms();

		return new RomService(backend.storage(), index);
	}


	// ===== Internal Helpers ==============================

	private RomService(StorageRegistry storage, RomIndex index)
	{
		super(storage, index, RomIndex.Record::filter, RomIndex.Record::filter);
	}
}
