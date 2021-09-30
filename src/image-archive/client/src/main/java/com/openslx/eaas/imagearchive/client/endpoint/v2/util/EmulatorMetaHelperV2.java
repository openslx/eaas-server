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

package com.openslx.eaas.imagearchive.client.endpoint.v2.util;

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.ImageArchiveMappers;
import com.openslx.eaas.imagearchive.api.v2.databind.MetaDataKindV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.AliasesV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.ArchiveV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.MetaDataV2;
import com.openslx.eaas.imagearchive.databind.EmulatorMetaData;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.logging.Logger;
import java.util.stream.Collectors;


public class EmulatorMetaHelperV2
{
	private final MetaDataV2 emulators;
	private final AliasesV2 aliases;


	public EmulatorMetaHelperV2(ImageArchiveClient client, Logger logger)
	{
		this(client.api().v2(), logger);
	}

	public EmulatorMetaHelperV2(ArchiveV2 archive, Logger logger)
	{
		this.emulators = archive.metadata(MetaDataKindV2.EMULATORS);
		this.aliases = archive.aliases();
	}

	public boolean exists(String name, String version) throws BWFLAException
	{
		return emulators.exists(EmulatorMetaData.identifier(name, version));
	}

	public EmulatorMetaData fetch(String name, String version) throws BWFLAException
	{
		final var id = EmulatorMetaData.identifier(name, version);
		return emulators.fetch(id, ImageArchiveMappers.JSON_TREE_TO_EMULATOR_METADATA);
	}

	public void insert(EmulatorMetaData emulator) throws BWFLAException
	{
		if (!this.exists(emulator.name(), EmulatorMetaData.DEFAULT_VERSION)) {
			emulator.tags()
					.add(EmulatorMetaData.DEFAULT_VERSION);
		}

		this.store(emulator);
	}

	public void markAsDefault(String name, String version) throws BWFLAException
	{
		this.markAsDefault(this.fetch(name, version));
	}

	public void markAsDefault(String id) throws BWFLAException
	{
		this.markAsDefault(emulators.fetch(id, ImageArchiveMappers.JSON_TREE_TO_EMULATOR_METADATA));
	}


	// ===== Internal Helpers ====================

	private void markAsDefault(EmulatorMetaData newDefaultEmulator) throws BWFLAException
	{
		// mark requested emulator as default...
		newDefaultEmulator.tags()
				.add(EmulatorMetaData.DEFAULT_VERSION);

		// update currently default emulator entry...
		final var alias = EmulatorMetaData.identifier(newDefaultEmulator.name(), EmulatorMetaData.DEFAULT_VERSION);
		if (emulators.exists(alias)) {
			final var curDefaultEmulator = emulators.fetch(alias, ImageArchiveMappers.JSON_TREE_TO_EMULATOR_METADATA);
			curDefaultEmulator.tags()
					.remove(EmulatorMetaData.DEFAULT_VERSION);

			this.store(curDefaultEmulator);
		}

		this.store(newDefaultEmulator);
	}

	private void store(EmulatorMetaData emulator) throws BWFLAException
	{
		final var altids = emulator.aliases()
				.collect(Collectors.toSet());

		if (!altids.isEmpty())
			aliases.replace(emulator.id(), altids);
		else if (aliases.exists(emulator.id()))
			aliases.delete(emulator.id());

		emulators.replace(emulator.id(), emulator, ImageArchiveMappers.OBJECT_TO_JSON_TREE);
	}
}
