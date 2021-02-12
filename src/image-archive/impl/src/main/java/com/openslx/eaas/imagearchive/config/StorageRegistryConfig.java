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
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class StorageRegistryConfig extends BaseConfig<StorageRegistryConfig>
{
	private List<StorageEndpointConfig> endpoints = new ArrayList<>();
	private List<StorageLocationConfig> locations = new ArrayList<>();
	private String defaultLocationName;


	// ===== Getters and Setters ====================

	@Config("default_location")
	public void setDefaultLocation(String name)
	{
		ConfigHelpers.check(name, "Default location-name is invalid!");
		this.defaultLocationName = name;
	}

	public String getDefaultLocation()
	{
		return defaultLocationName;
	}

	public void setEndpointConfigs(List<StorageEndpointConfig> endpoints)
	{
		ConfigHelpers.check(endpoints, "List of storage endpoints is invalid!");
		this.endpoints = endpoints;
	}

	public List<StorageEndpointConfig> getEndpointConfigs()
	{
		return endpoints;
	}

	public void setLocationConfigs(List<StorageLocationConfig> locations)
	{
		ConfigHelpers.check(locations, "List of storage locations is invalid!");
		this.locations = locations;
	}

	public List<StorageLocationConfig> getLocationConfigs()
	{
		return locations;
	}


	// ===== Initialization ====================

	@Override
	protected StorageRegistryConfig load(Configuration config, Logger log)
	{
		super.load(config, log);

		// Configure storage endpoints
		{
			endpoints.clear();

			while (true) {
				final String prefix = ConfigHelpers.toListKey("endpoints", endpoints.size(), ".");
				final Configuration usrconfig = ConfigHelpers.filter(config, prefix);
				if (usrconfig.get("name") == null)
					break;  // No more endpoints found!

				final var endpoint = new StorageEndpointConfig()
						.load(usrconfig, log);

				endpoints.add(endpoint);
			}

			log.info("Loaded " + endpoints.size() + " storage endpoint(s)");
		}

		// Configure storage locations
		{
			locations.clear();

			while (true) {
				final String prefix = ConfigHelpers.toListKey("locations", locations.size(), ".");
				final Configuration usrconfig = ConfigHelpers.filter(config, prefix);
				if (usrconfig.get("name") == null)
					break;  // No more locations found!

				// Compute combined view from user and default configurations
				final Configuration defaults = ConfigHelpers.filter(config, "locations.defaults.");
				final Configuration combined = ConfigHelpers.combine(defaults, usrconfig);

				final var location = new StorageLocationConfig()
						.load(combined, log);

				locations.add(location);
			}

			log.info("Loaded " + locations.size() + " storage location(s)");
		}

		return this;
	}
}
