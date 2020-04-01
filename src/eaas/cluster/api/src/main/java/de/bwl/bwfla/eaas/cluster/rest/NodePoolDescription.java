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
import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;

import java.util.Collection;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodePoolDescription
{
	private int numNodesUnused = -1;
	private int numNodesUnhealthy = -1;

	private ResourceSpec capacity = null;
	private ResourceSpec pending = null;
	private ResourceSpec free = null;

	private Collection<NodeDescription> nodes = null;


	public NodePoolDescription setNumNodesUnused(int num)
	{
		this.numNodesUnused = num;
		return this;
	}

	public NodePoolDescription setNumNodesUnhealthy(int num)
	{
		this.numNodesUnhealthy = num;
		return this;
	}

	public NodePoolDescription setCapacity(ResourceSpec capacity)
	{
		this.capacity = capacity;
		return this;
	}

	public NodePoolDescription setFreeResources(ResourceSpec free)
	{
		this.free = free;
		return this;
	}

	public NodePoolDescription setPendingResources(ResourceSpec pending)
	{
		this.pending = pending;
		return this;
	}

	public NodePoolDescription setNodes(Collection<NodeDescription> nodes)
	{
		this.nodes = nodes;
		return this;
	}

	@JsonProperty("num_nodes")
	public int getNumNodes()
	{
		return (nodes != null) ? nodes.size() : -1;
	}

	@JsonProperty("num_nodes_unused")
	public int getNumNodesUnused()
	{
		return numNodesUnused;
	}

	@JsonProperty("num_nodes_unhealthy")
	public int getNumNodesUnhealthy()
	{
		return numNodesUnhealthy;
	}

	@JsonProperty("capacity")
	public ResourceSpec getCapacity()
	{
		return capacity;
	}

	@JsonProperty("resources_pending")
	public ResourceSpec getPendingResources()
	{
		return pending;
	}

	@JsonProperty("resources_free")
	public ResourceSpec getFreeResources()
	{
		return free;
	}

	@JsonProperty("resources_allocated")
	public ResourceSpec getAllocatedResources()
	{
		return MutableResourceSpec.fromDiff(capacity, free);
	}

	@JsonProperty("nodes")
	public Collection<NodeDescription> getNodes()
	{
		return nodes;
	}
}
