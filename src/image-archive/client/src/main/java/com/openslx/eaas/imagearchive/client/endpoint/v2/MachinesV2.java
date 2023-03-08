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

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.IMachinesV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.RemoteResourceRWM;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;


public class MachinesV2 extends RemoteResourceRWM<MachineConfiguration, IMachinesV2>
{
	public MachinesV2(ImageArchiveClient.Context context, IMachinesV2 api)
	{
		super(context, api);
	}

	@Override
	public Class<MachineConfiguration> getTargetClass()
	{
		return MachineConfiguration.class;
	}
}
