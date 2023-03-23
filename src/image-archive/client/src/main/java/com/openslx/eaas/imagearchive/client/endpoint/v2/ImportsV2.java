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
import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.IImportsV2;
import com.openslx.eaas.imagearchive.api.v2.common.FetchOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportRequestV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportStateV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportStatusV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.IDeletableResource;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.IListableResource;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.IReadableResource;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.RemoteResource;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class ImportsV2 extends RemoteResource<IImportsV2>
		implements IListableResource, IReadableResource<ImportStatusV2>, IDeletableResource
{
	public ImportsV2(ImageArchiveClient.Context context, IImportsV2 api)
	{
		super(context, api);
	}


	// ===== Public API ==============================

	public Streamable<ImportStatusV2> fetch() throws BWFLAException
	{
		return this.fetch((FetchOptionsV2) null);
	}

	public Streamable<ImportStatusV2> fetch(FetchOptionsV2 options) throws BWFLAException
	{
		final var response = api.fetch(options);
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

	/** Submit import-request and wait for result or timeout */
	public String await(ImportRequestV2 request, long timeout, TimeUnit unit) throws Exception
	{
		final var taskid = this.insert(request);
		try {
			final var status = this.watch(taskid)
					.get(timeout, unit);

			if (status.state() != ImportStateV2.FINISHED)
				throw new BWFLAException("Importing image failed!");

			return status.target()
					.name();
		}
		catch (TimeoutException error) {
			// try to abort task!
			this.delete(taskid);
			throw error;
		}
	}
}
