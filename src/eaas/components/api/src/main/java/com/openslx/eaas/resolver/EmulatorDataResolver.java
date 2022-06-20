/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.resolver;

import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;


public class EmulatorDataResolver extends DataResolver
{
	public EmulatorDataResolver()
	{
		super(DataResolver.getDefaultEndpoint());
	}

	public String resolve(String imageid)
	{
		return this.resolve(null, "emulators", imageid);
	}

	public String resolve(ImageArchiveBinding binding)
	{
		return this.resolve(binding.getImageId());
	}
}
