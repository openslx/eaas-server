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

import com.openslx.eaas.imagearchive.api.v2.ITemplatesV2;
import com.openslx.eaas.imagearchive.api.v2.common.IDeletable;
import com.openslx.eaas.imagearchive.api.v2.common.IListable;
import com.openslx.eaas.imagearchive.api.v2.common.IManyReadable;
import com.openslx.eaas.imagearchive.api.v2.common.IReadable;
import com.openslx.eaas.imagearchive.api.v2.common.IWritable;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.AbstractResourceRWM;
import de.bwl.bwfla.emucomp.api.MachineConfigurationTemplate;


public class TemplatesV2 extends AbstractResourceRWM<MachineConfigurationTemplate>
{
	private final ITemplatesV2 api;

	public TemplatesV2(ITemplatesV2 api)
	{
		super(MachineConfigurationTemplate.class);
		this.api = api;
	}


	// ===== Internal Helpers ==============================

	@Override
	protected IListable listable()
	{
		return api;
	}

	@Override
	protected IReadable<MachineConfigurationTemplate> readable()
	{
		return api;
	}

	@Override
	protected IManyReadable<MachineConfigurationTemplate> manyreadable()
	{
		return api;
	}

	@Override
	protected IWritable<MachineConfigurationTemplate> writable()
	{
		return api;
	}

	@Override
	protected IDeletable deletable()
	{
		return api;
	}
}
