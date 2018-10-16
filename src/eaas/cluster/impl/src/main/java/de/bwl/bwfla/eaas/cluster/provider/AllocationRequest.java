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

package de.bwl.bwfla.eaas.cluster.provider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;

// package-private

class AllocationRequest
{
	private final CompletableFuture<ResourceHandle> result;
	private final UUID allocationId;
    private final ResourceSpec spec;
	private final long deadline;
	
	public AllocationRequest(CompletableFuture<ResourceHandle> result, UUID allocationId, ResourceSpec spec, long deadline)
	{
		this.result = result;
		this.allocationId = allocationId;
		this.spec = spec;
		this.deadline = deadline;
	}
	
    public UUID getAllocationId()
    {
        return allocationId;
    }
    
    public ResourceSpec getResourceSpec()
	{
		return spec;
	}
	
	public long getDeadline()
	{
		return deadline;
	}
	
	public void complete(ResourceHandle handle)
	{
		result.complete(handle);
	}
	
	public void abort(Throwable error)
	{
		result.completeExceptionally(error);
	}
	
	public void cancel()
	{
		result.cancel(false);
	}
}
