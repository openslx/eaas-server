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

package de.bwl.bwfla.eaas.cluster.provider.allocation;

import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;

// package-private

class ResourceAllocation implements Comparable<ResourceAllocation>
{
	private final ResourceHandle handle;
	private final ResourceSpec resource;
	
	public ResourceAllocation(ResourceHandle handle, ResourceSpec resource)
	{
		this.handle = handle;
		this.resource = resource;
	}
	
	public ResourceSpec getResourceSpec()
	{
		return resource;
	}
	
	public ResourceHandle getHandle()
	{
		return handle;
	}

	@Override
	public int compareTo(ResourceAllocation other)
	{
		return handle.compareTo(other.handle);
	}
}