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
import com.openslx.eaas.imagearchive.indexing.impl.TemplateIndex;
import com.openslx.eaas.imagearchive.service.DataService;
import com.openslx.eaas.imagearchive.storage.StorageRegistry;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.MachineConfigurationTemplate;


public class TemplateService extends DataService<MachineConfigurationTemplate, TemplateIndex.Record>
{
	public static TemplateService create(ArchiveBackend backend)
	{
		final var index = backend.indexes()
				.templates();

		return new TemplateService(backend.storage(), index);
	}


	// ===== Internal Helpers ==============================

	private TemplateService(StorageRegistry storage, TemplateIndex index)
	{
		super(storage, index, TemplateIndex.Record::filter, TemplateIndex.Record::filter);
	}

	@Override
	protected String update(String location, String id, MachineConfigurationTemplate template) throws BWFLAException
	{
		if (id == null)
			id = this.nextid();

		// update templates's metadata
		template.setId(id);

		return super.update(location, id, template);
	}
}
