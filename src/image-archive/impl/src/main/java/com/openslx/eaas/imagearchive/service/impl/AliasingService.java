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

package com.openslx.eaas.imagearchive.service.impl;

import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.databind.AliasingDescriptor;
import com.openslx.eaas.imagearchive.indexing.impl.AliasingIndex;
import com.openslx.eaas.imagearchive.service.AbstractService;
import com.openslx.eaas.imagearchive.service.BlobService;
import com.openslx.eaas.imagearchive.service.DataService;
import com.openslx.eaas.imagearchive.service.ServiceRegistry;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.function.Consumer;
import java.util.function.Predicate;


public class AliasingService extends DataService<AliasingDescriptor, AliasingIndex.Record>
{
	private final ServiceRegistry services;


	public static AliasingService create(ArchiveBackend backend, ServiceRegistry services)
	{
		final var index = backend.indexes()
				.aliases();

		return new AliasingService(backend.storage(), services, index);
	}

	@Override
	public boolean remove(String id) throws BWFLAException
	{
		final var removed = super.remove(id);
		this.updateReferences(id, null);
		return removed;
	}


	// ===== Internal Helpers ==============================

	private AliasingService(StorageRegistry storage, ServiceRegistry services, AliasingIndex index)
	{
		super(storage, index, AliasingIndex.Record::filter, AliasingIndex.Record::filter);
		this.services = services;
	}

	@Override
	protected String update(String location, String id, AliasingDescriptor data) throws BWFLAException
	{
		if (id == null)
			id = this.nextid();

		// update target name
		data.setName(id);

		super.update(location, id, data);
		this.updateReferences(id, data);
		return id;
	}

	private void updateReferences(String id, AliasingDescriptor aliasing)
	{
		final Predicate<AbstractService<?>> predicate = (service) -> {
			if (service instanceof AliasingService)
				return false;

			return true;
		};

		final Consumer<AbstractService<?>> updater = (service) -> {
			if (service instanceof BlobService<?>)
				((BlobService<?>) service).update(id, aliasing);
		};

		// update all inlined aliases
		services.stream()
				.filter(predicate)
				.forEach(updater);
	}
}
