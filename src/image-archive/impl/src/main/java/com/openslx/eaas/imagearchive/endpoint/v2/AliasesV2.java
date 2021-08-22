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

import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.api.v2.IAliasesV2;
import com.openslx.eaas.imagearchive.databind.AliasingDescriptor;
import com.openslx.eaas.imagearchive.endpoint.v2.common.MappableResource;
import com.openslx.eaas.imagearchive.indexing.impl.AliasingIndex;
import com.openslx.eaas.imagearchive.service.impl.AliasingService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Set;


@ApplicationScoped
public class AliasesV2 extends MappableResource<Set<String>, AliasingDescriptor, AliasingIndex.Record>
		implements IAliasesV2
{
	private AliasingService service;


	// ===== Internal Helpers ==============================

	@PostConstruct
	private void initialize()
	{
		this.service = ArchiveBackend.instance()
				.services()
				.aliases();
	}

	@Override
	protected AliasingService service()
	{
		return service;
	}

	@Override
	protected AliasingDescriptor map(String id, Set<String> aliases)
	{
		return new AliasingDescriptor()
				.setName(id)
				.setAliases(aliases);
	}

	@Override
	protected Set<String> map(AliasingDescriptor descriptor)
	{
		return descriptor.aliases();
	}
}
