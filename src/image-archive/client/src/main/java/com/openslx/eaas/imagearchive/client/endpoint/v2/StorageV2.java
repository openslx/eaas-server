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
import com.openslx.eaas.imagearchive.api.v2.IStorageV2;


public class StorageV2
{
	private final LocationsV2 locations;
	private final IndexesV2 indexes;

	public StorageV2(ImageArchiveClient.Context context, IStorageV2 api)
	{
		this.locations = new LocationsV2(StorageV2.resolve(context, "locations"), api.locations());
		this.indexes = new IndexesV2(StorageV2.resolve(context, "indexes"), api.indexes());
	}


	// ===== Public API ==============================

	public LocationsV2 locations()
	{
		return locations;
	}

	public IndexesV2 indexes()
	{
		return indexes;
	}


	// ===== Internal Helpers ====================

	private static ImageArchiveClient.Context resolve(ImageArchiveClient.Context context, String methodname)
	{
		return context.clone()
				.resolve(IStorageV2.class, methodname);
	}
}
