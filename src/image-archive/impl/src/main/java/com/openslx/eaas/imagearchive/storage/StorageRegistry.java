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

package com.openslx.eaas.imagearchive.storage;

import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.config.StorageRegistryConfig;
import com.openslx.eaas.imagearchive.config.StorageEndpointConfig;
import com.openslx.eaas.imagearchive.config.StorageLocationConfig;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;


public class StorageRegistry implements AutoCloseable
{
	private final Map<String, StorageEndpoint> endpoints;
	private final Map<String, StorageLocation> locations;
	private final StorageRegistryConfig config;


	public Map<String, StorageEndpoint> endpoints()
	{
		return endpoints;
	}

	public Map<String, StorageLocation> locations()
	{
		return locations;
	}

	public StorageRegistryConfig config()
	{
		return config;
	}

	@Override
	public void close() throws Exception
	{
		locations.forEach((name, location) -> StorageRegistry.close(location, "location"));
		endpoints.forEach((name, endpoint) -> StorageRegistry.close(endpoint, "endpoint"));
	}

	public static StorageRegistry create(StorageRegistryConfig config) throws BWFLAException
	{
		return new StorageRegistry(config);
	}


	// ===== Internal Helpers ==============================

	private StorageRegistry(StorageRegistryConfig config) throws BWFLAException
	{
		this.endpoints = new LinkedHashMap<>();
		this.locations = new LinkedHashMap<>();
		this.config = config;

		final var log = ArchiveBackend.logger();

		log.info("Initializing storage endpoints...");
		for (StorageEndpointConfig sec : config.getEndpointConfigs())
			endpoints.put(sec.getName(), new StorageEndpoint(sec));

		log.info("Initialized " + endpoints.size() + " storage endpoint(s)");

		log.info("Initializing storage locations...");
		for (StorageLocationConfig slc : config.getLocationConfigs()) {
			final var endpoint = endpoints.get(slc.getEndpoint());
			if (endpoint == null)
				throw new BWFLAException("Unknown endpoint specified: " + slc.getEndpoint());

			locations.put(slc.getName(), new StorageLocation(slc, endpoint));
		}

		log.info("Initialized " + locations.size() + " storage location(s)");
	}

	private static void close(AutoCloseable object, String name)
	{
		try {
			object.close();
		}
		catch (Exception error) {
			ArchiveBackend.logger()
					.log(Level.WARNING, "Closing storage " + name + " failed!", error);
		}
	}
}
