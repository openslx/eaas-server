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

package com.openslx.eaas.imagearchive.config;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;

import java.util.logging.Logger;


public class ImageArchiveConfig extends BaseConfig<ImageArchiveConfig>
{
	private StorageRegistryConfig storage = new StorageRegistryConfig();


	// ===== Getters and Setters ====================

	public void setStorageConfig(StorageRegistryConfig storage)
	{
		ConfigHelpers.check(storage, "Storage config is invalid!");
		this.storage = storage;
	}

	public StorageRegistryConfig getStorageConfig()
	{
		return storage;
	}


	// ===== Initialization ====================

	public static ImageArchiveConfig create(Logger log)
	{
		return new ImageArchiveConfig()
				.load(log);
	}


	// ===== Internal Helpers ====================

	private ImageArchiveConfig()
	{
		// Empty!
	}

	@Override
	protected ImageArchiveConfig load(Configuration config, Logger log) throws ConfigException
	{
		log.info("Loading image-archive's configuration...");
		storage.load(ConfigHelpers.filter(config,"imagearchive.storage."), log);
		return super.load(config, log);
	}
}
