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
import com.openslx.eaas.imagearchive.api.v2.common.FetchOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.InsertOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ResolveOptionsV2;
import com.openslx.eaas.imagearchive.indexing.DataRecord;
import com.openslx.eaas.imagearchive.service.DataService;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;


public abstract class MappableResource<T, D, R extends DataRecord<D>> extends AbstractResource<R>
{
	// ===== IManyReadable API ==============================

	public String resolve(String id, ResolveOptionsV2 options) throws BWFLAException
	{
		final var method = BlobResource.convert(options.method());
		final var result = this.service()
				.resolve(id, options.lifetime(), method);

		if (result == null)
			throw new NotFoundException();

		return result;
	}

	public T fetch(String id) throws BWFLAException
	{
		final var result = this.service()
				.lookup(id);

		if (result == null)
			throw new NotFoundException();

		return this.map(result.data());
	}

	public Response fetch(FetchOptionsV2 options) throws BWFLAException
	{
		final var result = this.service()
				.list(MappableResource.convert(options), options.offset(), options.limit())
				.map((record) -> this.map(record.data()));

		return Response.ok(Streamable.of(result))
				.build();
	}


	// ===== IWritable API ==============================

	public String insert(T value, InsertOptionsV2 options) throws BWFLAException
	{
		final var data = this.map(null, value);
		return this.service()
				.insert(options.location(), data);
	}

	public void replace(String id, T value, ReplaceOptionsV2 options) throws BWFLAException
	{
		final var data = this.map(id, value);
		this.service()
				.replace(options.location(), id, data);
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

	protected abstract DataService<D,R> service();
	protected abstract D map(String id, T value);
	protected abstract T map(D value);
}
