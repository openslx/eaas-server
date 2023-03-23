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

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.IArchiveV2;
import com.openslx.eaas.imagearchive.api.v2.databind.MetaDataKindV2;

import java.util.HashMap;
import java.util.Map;


public class ArchiveV2
{
	private final AliasesV2 aliases;
	private final ContainersV2 containers;
	private final MachinesV2 machines;
	private final TemplatesV2 templates;
	private final CheckpointsV2 checkpoints;
	private final EmulatorsV2 emulators;
	private final ImagesV2 images;
	private final RomsV2 roms;
	private final ImportsV2 imports;
	private final StorageV2 storage;

	// virtual endpoints
	private final EnvironmentsV2 environments;

	// parameterized endpoints
	private final Map<String, MetaDataV2> metadata;

	public ArchiveV2(ImageArchiveClient.Context context, IArchiveV2 api)
	{
		this.aliases = new AliasesV2(ArchiveV2.resolve(context, "aliases"), api.aliases());
		this.containers = new ContainersV2(ArchiveV2.resolve(context, "containers"), api.containers());
		this.machines = new MachinesV2(ArchiveV2.resolve(context, "machines"), api.machines());
		this.templates = new TemplatesV2(ArchiveV2.resolve(context, "templates"), api.templates());
		this.checkpoints = new CheckpointsV2(ArchiveV2.resolve(context, "checkpoints"), api.checkpoints());
		this.emulators = new EmulatorsV2(ArchiveV2.resolve(context, "emulators"), api.emulators());
		this.images = new ImagesV2(ArchiveV2.resolve(context, "images"), api.images());
		this.roms = new RomsV2(ArchiveV2.resolve(context, "roms"), api.roms());
		this.imports = new ImportsV2(ArchiveV2.resolve(context, "imports"), api.imports());
		this.storage = new StorageV2(ArchiveV2.resolve(context, "storage"), api.storage());
		this.environments = new EnvironmentsV2(this, context.logger());

		this.metadata = new HashMap<>();
		for (final var kind : MetaDataKindV2.values()) {
			final var subctx = ArchiveV2.resolve(context, "metadata")
					.resolve("kind", kind.value());

			metadata.put(kind.value(), new MetaDataV2(subctx, api.metadata(kind.value())));
		}
	}


	// ===== Public API ==============================

	public AliasesV2 aliases()
	{
		return aliases;
	}

	public ContainersV2 containers()
	{
		return containers;
	}

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

	public EmulatorsV2 emulators()
	{
		return emulators;
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

	public MetaDataV2 metadata(MetaDataKindV2 kind)
	{
		return this.metadata(kind.value());
	}

	public MetaDataV2 metadata(String kind)
	{
		final var api = metadata.get(kind);
		if (api == null)
			throw new IllegalArgumentException();

		return api;
	}


	// ===== Internal Helpers ====================

	private static ImageArchiveClient.Context resolve(ImageArchiveClient.Context context, String methodname)
	{
		return context.clone()
				.resolve(IArchiveV2.class, methodname);
	}
}
