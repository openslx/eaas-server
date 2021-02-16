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

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.indexing.BlobDescriptor;
import com.openslx.eaas.imagearchive.service.AbstractService;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;


public abstract class AbstractResource<T extends BlobDescriptor>
{
	// ===== IListable API ==============================

	public long count()
	{
		return this.service()
				.count();
	}

	public void exists(String id) throws BWFLAException
	{
		final var result = this.service()
				.lookup(id);

		if (result == null)
			throw new NotFoundException();
	}

	public Response list(int offset, int limit) throws BWFLAException
	{
		final var result = this.service()
				.list(offset, limit)
				.map(T::name);

		return Response.ok(Streamable.of(result))
				.build();
	}


	// ===== Internal Helpers ==============================

	protected abstract AbstractService<T> service();
}
