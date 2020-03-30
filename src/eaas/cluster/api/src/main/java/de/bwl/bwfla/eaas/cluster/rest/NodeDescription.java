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

package de.bwl.bwfla.eaas.cluster.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;

import java.util.Collection;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeDescription
{
	private final String id;
	private boolean used = false;
	private boolean healthy = false;
	private ResourceSpec capacity = null;
	private ResourceSpec utilization = null;
	private Collection<AllocationDescription> allocations = null;


	public NodeDescription(String id)
	{
		this.id = id;
	}

	public NodeDescription setUsedFlag(boolean used)
	{
		this.used = used;
		return this;
	}

	public NodeDescription setHealthyFlag(boolean healthy)
	{
		this.healthy = healthy;
		return this;
	}

	public NodeDescription setCapacity(ResourceSpec capacity)
	{
		this.capacity = capacity;
		return this;
	}

	public NodeDescription setUtilization(ResourceSpec utilization)
	{
		this.utilization = utilization;
		return this;
	}

	public NodeDescription setAllocations(Collection<AllocationDescription> allocations)
	{
		this.allocations = allocations;
		return this;
	}

	@JsonProperty("id")
	public String getId()
	{
		return id;
	}

	@JsonProperty("is_used")
	public boolean isUsed()
	{
		return used;
	}

	@JsonProperty("is_healthy")
	public boolean isHealthy()
	{
		return healthy;
	}

	@JsonProperty("capacity")
	public ResourceSpec getCapacity()
	{
		return capacity;
	}

	@JsonProperty("utilization")
	public ResourceSpec getUtilization()
	{
		return utilization;
	}

	@JsonProperty("num_allocations")
	public int getNumAllocations()
	{
		return (allocations != null) ? allocations.size() : 0;
	}

	@JsonProperty("allocations")
	public Collection<AllocationDescription> getAllocations()
	{
		return allocations;
	}
}
