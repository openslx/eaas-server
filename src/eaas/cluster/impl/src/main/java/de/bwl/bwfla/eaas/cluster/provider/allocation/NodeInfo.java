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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.eaas.cluster.IDescribable;
import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;
import de.bwl.bwfla.eaas.cluster.provider.Node;
import de.bwl.bwfla.eaas.cluster.rest.AllocationDescription;
import de.bwl.bwfla.eaas.cluster.rest.NodeDescription;

// package-private

class NodeInfo implements IDumpable, IDescribable<NodeDescription>
{
	private final Node node;
	private final MutableResourceSpec freeResources;
	private final Map<UUID, ResourceAllocation> allocations;
	
	public NodeInfo(Node node)
	{
		this.node = node;
		this.freeResources = new MutableResourceSpec(node.getCapacity());
		this.allocations = new HashMap<UUID, ResourceAllocation>();
	}
	
	public NodeID getNodeID()
	{
		return node.getId();
	}
	
	public ResourceSpec getUsedResources()
	{
		return MutableResourceSpec.fromDiff(node.getCapacity(), freeResources);
	}
	
	public ResourceSpec getFreeResources()
	{
		return freeResources;
	}
	
	public ResourceSpec getCapacity()
	{
		return node.getCapacity();
	}
	
	public int getNumAllocations()
	{
		return allocations.size();
	}
	
	public boolean isHealthy()
	{
		return node.isHealthy();
	}
	
	public void addAllocation(ResourceAllocation allocation)
	{
		ResourceSpec spec = allocation.getResourceSpec();
		if (!freeResources.reserve(spec))
			throw new IllegalStateException("Requested resources are not available!");
		
		UUID aid = allocation.getHandle().getAllocationID();
		allocations.put(aid, allocation);
		if (allocations.size() == 1)
			node.setUsed(true);
	}
	
	public ResourceAllocation removeAllocation(ResourceHandle handle)
	{
		ResourceAllocation allocation = allocations.remove(handle.getAllocationID());
		if (allocation != null)
			freeResources.free(allocation.getResourceSpec());
		
		if (allocations.isEmpty())
			node.setUsed(false);
		
		return allocation;
	}

	@Override
	public NodeDescription describe(boolean detailed)
	{
		final Collection<AllocationDescription> adcol = allocations.values()
				.stream()
				.map((allocation) -> {
					final UUID aid = allocation.getHandle().getAllocationID();
					return new AllocationDescription(aid.toString())
							.setResourceSpec(allocation.getResourceSpec());
				})
				.collect(Collectors.toList());

		return new NodeDescription(node.getId().toString())
				.setCapacity(this.getCapacity())
				.setUtilization(this.getUsedResources())
				.setHealthyFlag(node.isHealthy())
				.setUsedFlag(node.isUsed())
				.setAllocations(adcol);
	}
	
	@Override
	public int hashCode()
	{
		return this.getNodeID().hashCode();
	}

	@Override
	public boolean equals(Object other)
	{
		final NodeID thisId = this.getNodeID();
		final NodeID otherId = ((NodeInfo) other).getNodeID();
		return thisId.equals(otherId);
	}

	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.ID, () -> json.write(DumpFields.ID, node.getId().toString()));
			dumper.add(DumpFields.FREE_RESOURCES, () -> {
				json.write(DumpFields.FREE_RESOURCES, DumpHelpers.toJsonObject(freeResources));
			});
			
			dumper.add(DumpFields.ALLOCATIONS, () -> {
				json.write("num_" + DumpFields.ALLOCATIONS, allocations.size());
				json.writeStartArray(DumpFields.ALLOCATIONS);
				for (ResourceAllocation allocation : allocations.values()) {
					json.writeStartObject();
					json.write("id", allocation.getHandle().getAllocationID().toString());
					json.write("spec", DumpHelpers.toJsonObject(allocation.getResourceSpec()));
					json.writeEnd();
				}

				json.writeEnd();
			});

			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String ID              = "id";
		private static final String FREE_RESOURCES  = "free_resources";
		private static final String ALLOCATIONS     = "allocations";
	}
}
