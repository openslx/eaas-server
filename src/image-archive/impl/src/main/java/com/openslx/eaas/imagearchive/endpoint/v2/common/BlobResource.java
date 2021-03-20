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

package com.openslx.eaas.imagearchive.endpoint.v2.common;

import com.openslx.eaas.imagearchive.api.v2.common.InsertOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ResolveOptionsV2;
import com.openslx.eaas.imagearchive.indexing.BlobDescriptor;
import com.openslx.eaas.imagearchive.service.BlobService;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.ws.rs.NotFoundException;
import java.io.InputStream;


public abstract class BlobResource<T extends BlobDescriptor> extends AbstractResource<T>
{
	// ===== IReadable API ==============================

	public String resolve(String id, ResolveOptionsV2 options) throws BWFLAException
	{
		return this.service()
				.resolve(id, options.lifetime());
	}

	public InputStream fetch(String id) throws BWFLAException
	{
		final var result = this.service()
				.download(id);

		if (result == null)
			throw new NotFoundException();

		return result;
	}


	// ===== IWritable API ==============================

	public String insert(InputStream value, InsertOptionsV2 options) throws BWFLAException
	{
		return this.service()
				.upload(options.location(), value);
	}

	public void replace(String id, InputStream value, ReplaceOptionsV2 options) throws BWFLAException
	{
		this.service()
				.upload(options.location(), id, value, BlobService.UNKNOWN_SIZE);
	}


	// ===== IDeletable API ==============================

	public void delete(String id) throws BWFLAException
	{
		final var deleted = this.service()
				.remove(id);

		if (!deleted)
			throw new NotFoundException();
	}


	// ===== Internal Helpers ==============================

	protected abstract BlobService<T> service();
}
