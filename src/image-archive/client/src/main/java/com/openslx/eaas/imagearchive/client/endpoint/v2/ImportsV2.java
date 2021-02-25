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

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.api.v2.IImportsV2;
import com.openslx.eaas.imagearchive.api.v2.common.IListable;
import com.openslx.eaas.imagearchive.api.v2.common.IReadable;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportRequestV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportStatusV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.AbstractResourceRO;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.concurrent.CompletableFuture;


public class ImportsV2 extends AbstractResourceRO<ImportStatusV2>
{
	private final IImportsV2 api;

	public ImportsV2(IImportsV2 api)
	{
		this.api = api;
	}


	// ===== Public API ==============================

	public Streamable<ImportStatusV2> fetch() throws BWFLAException
	{
		return this.fetch(0, Integer.MAX_VALUE);
	}

	public Streamable<ImportStatusV2> fetch(int offset, int limit) throws BWFLAException
	{
		final var response = api.fetch(offset, limit);
		return Streamable.of(response, ImportStatusV2.class);
	}

	public String insert(ImportRequestV2 request) throws BWFLAException
	{
		return api.insert(request);
	}

	public void delete(String id) throws BWFLAException
	{
		api.delete(id);
	}

	public CompletableFuture<ImportStatusV2> watch(String id) throws BWFLAException
	{
		return api.watch(id)
				.toCompletableFuture();
	}


	// ===== Internal Helpers ==============================

	@Override
	protected IListable listable()
	{
		return api;
	}

	@Override
	protected IReadable<ImportStatusV2> readable()
	{
		return api;
	}
}
