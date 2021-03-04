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

package com.openslx.eaas.imagearchive.client.endpoint.v2;

import com.openslx.eaas.imagearchive.api.v2.IArchiveV2;

import java.util.logging.Logger;


public class ArchiveV2
{
	private final MachinesV2 machines;
	private final TemplatesV2 templates;
	private final CheckpointsV2 checkpoints;
	private final ImagesV2 images;
	private final RomsV2 roms;
	private final ImportsV2 imports;
	private final StorageV2 storage;

	// virtual endpoints
	private final EnvironmentsV2 environments;


	public ArchiveV2(IArchiveV2 api, Logger logger)
	{
		this.machines = new MachinesV2(api.machines());
		this.templates = new TemplatesV2(api.templates());
		this.checkpoints = new CheckpointsV2(api.checkpoints());
		this.images = new ImagesV2(api.images());
		this.roms = new RomsV2(api.roms());
		this.imports = new ImportsV2(api.imports());
		this.storage = new StorageV2(api.storage());
		this.environments = new EnvironmentsV2(this, logger);
	}


	// ===== Public API ==============================

	public MachinesV2 machines()
	{
		return machines;
	}

	public TemplatesV2 templates()
	{
		return templates;
	}

	public CheckpointsV2 checkpoints()
	{
		return checkpoints;
	}

	public ImagesV2 images()
	{
		return images;
	}

	public RomsV2 roms()
	{
		return roms;
	}

	public ImportsV2 imports()
	{
		return imports;
	}

	public StorageV2 storage()
	{
		return storage;
	}

	public EnvironmentsV2 environments()
	{
		return environments;
	}
}
