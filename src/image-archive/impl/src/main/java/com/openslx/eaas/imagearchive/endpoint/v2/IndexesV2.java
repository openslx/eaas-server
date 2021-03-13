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

package com.openslx.eaas.imagearchive.endpoint.v2;

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.BlobKind;
import com.openslx.eaas.imagearchive.api.v2.IIndexesV2;
import com.openslx.eaas.imagearchive.indexing.IndexRegistry;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;


@ApplicationScoped
public class IndexesV2 implements IIndexesV2
{
	private StorageRegistry storage;
	private IndexRegistry indexes;


	@Override
	public Response list() throws BWFLAException
	{
		final var names = indexes.list()
				.map(BlobKind::value);

		return Response.ok(Streamable.of(names))
				.build();
	}

	@Override
	public void exists(String name) throws BWFLAException
	{
		final var expkind = BlobKind.from(name);
		final var found = indexes.list()
				.anyMatch((curkind) -> curkind == expkind);

		if (!found)
			throw new NotFoundException();
	}

	@Override
	public void rebuild(String name) throws BWFLAException
	{
		final var kind = BlobKind.from(name);
		final var index = indexes.lookup(kind);
		if (index == null)
			throw new NotFoundException();

		index.rebuild(storage);
	}

	@Override
	public void rebuild() throws BWFLAException
	{
		indexes.rebuild(storage);
	}


	// ===== Internal Helpers ==============================

	@PostConstruct
	private void initialize()
	{
		final var backend = ArchiveBackend.instance();
		this.storage = backend.storage();
		this.indexes = backend.indexes();
	}
}
