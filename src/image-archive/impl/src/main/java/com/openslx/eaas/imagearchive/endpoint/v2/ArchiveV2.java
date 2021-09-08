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

package com.openslx.eaas.imagearchive.endpoint.v2;

import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.api.v2.IAliasesV2;
import com.openslx.eaas.imagearchive.api.v2.IArchiveV2;
import com.openslx.eaas.imagearchive.api.v2.ICheckpointsV2;
import com.openslx.eaas.imagearchive.api.v2.IContainersV2;
import com.openslx.eaas.imagearchive.api.v2.IImagesV2;
import com.openslx.eaas.imagearchive.api.v2.IImportsV2;
import com.openslx.eaas.imagearchive.api.v2.IMachinesV2;
import com.openslx.eaas.imagearchive.api.v2.IMetaDataV2;
import com.openslx.eaas.imagearchive.api.v2.IRomsV2;
import com.openslx.eaas.imagearchive.api.v2.IStorageV2;
import com.openslx.eaas.imagearchive.api.v2.ITemplatesV2;
import com.openslx.eaas.imagearchive.api.v2.databind.MetaDataKindV2;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;


@ApplicationScoped
public class ArchiveV2 implements IArchiveV2
{
	@Inject
	private AliasesV2 aliases;

	@Inject
	private ContainersV2 containers;

	@Inject
	private MachinesV2 machines;

	@Inject
	private TemplatesV2 templates;

	@Inject
	private CheckpointsV2 checkpoints;

	@Inject
	private ImagesV2 images;

	@Inject
	private RomsV2 roms;

	@Inject
	private ImportsV2 imports;

	@Inject
	private StorageV2 storage;


	// ===== Public API ==============================

	@Override
	public IAliasesV2 aliases()
	{
		return aliases;
	}

	@Override
	public IContainersV2 containers()
	{
		return containers;
	}

	@Override
	public IMachinesV2 machines()
	{
		return machines;
	}

	@Override
	public ITemplatesV2 templates()
	{
		return templates;
	}

	@Override
	public ICheckpointsV2 checkpoints()
	{
		return checkpoints;
	}

	@Override
	public IImagesV2 images()
	{
		return images;
	}

	@Override
	public IRomsV2 roms()
	{
		return roms;
	}

	@Override
	public IImportsV2 imports()
	{
		return imports;
	}

	@Override
	public IStorageV2 storage()
	{
		return storage;
	}

	@Override
	public IMetaDataV2 metadata(String kind)
	{
		final var services = ArchiveBackend.instance()
				.services();

		switch (MetaDataKindV2.from(kind)) {
			case IMAGES:
				return new MetaDataV2(services.imageMetaData());
			case ENVIRONMENTS:
				return new MetaDataV2(services.environments());
			case SESSIONS:
				return new MetaDataV2(services.sessions());
			case NETWORKS:
				return new MetaDataV2(services.networks());
			default:
				throw new BadRequestException();
		}
	}
}
