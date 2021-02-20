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

package com.openslx.eaas.imagearchive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openslx.eaas.common.databind.DataUtils;
import com.openslx.eaas.imagearchive.indexing.BlobIndex;
import com.openslx.eaas.imagearchive.indexing.DataRecord;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.io.ByteArrayInputStream;


public abstract class DataService<D, T extends DataRecord<D>> extends BlobService<T>
{
	/** Insert a new record into service */
	public String insert(D data) throws BWFLAException
	{
		return this.update(null, data);
	}

	/** Replace record with given data */
	public void replace(String id, D data) throws BWFLAException
	{
		this.update(id, data);
	}


	// ===== Internal Helpers ==============================

	protected DataService(StorageRegistry storage, BlobIndex<T> index, IdentifierFilter idfilter)
	{
		super(storage, index, idfilter);
	}

	private String update(String id, D data) throws BWFLAException
	{
		try {
			// upload serialized data to backend storage...
			final var bytes = DataUtils.json()
					.writer()
					.writeValueAsBytes(data);

			final var stream = new ByteArrayInputStream(bytes);
			if (id == null)
				id = this.upload(stream, stream.available());
			else this.upload(id, stream, stream.available());

			return id;
		}
		catch (JsonProcessingException error) {
			throw new BWFLAException("Serializing data failed!", error);
		}
	}
}
