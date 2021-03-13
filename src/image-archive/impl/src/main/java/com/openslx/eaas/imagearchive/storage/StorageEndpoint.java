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
import com.openslx.eaas.imagearchive.config.StorageEndpointConfig;
import de.bwl.bwfla.blobstore.BlobStore;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public class StorageEndpoint implements AutoCloseable
{
	private final StorageEndpointConfig config;
	private final BlobStore client;

	StorageEndpoint(StorageEndpointConfig config) throws BWFLAException
	{
		this.config = config;
		this.client = BlobStore.builder()
				.endpoint(config.getAddress())
				.credentials(config.getAccessKey(), config.getSecretKey())
				.logger(ArchiveBackend.logger("endpoint", config.getName()))
				.build();
	}

	public StorageEndpointConfig config()
	{
		return config;
	}

	public BlobStore client()
	{
		return client;
	}

	@Override
	public void close() throws Exception
	{
		// Empty!
	}
}
