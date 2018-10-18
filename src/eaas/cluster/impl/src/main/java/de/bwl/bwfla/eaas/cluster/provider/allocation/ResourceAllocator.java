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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.NotFoundException;

import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;
import de.bwl.bwfla.eaas.cluster.provider.Node;


public class ResourceAllocator implements IResourceAllocator, IDumpable
{
	private final Map<NodeID, NodeInfo> nodes;
	private final ResourceIndex index;
	private final MutableResourceSpec capacity;
	private final MutableResourceSpec available;
	private final String provider;
	private int numAllocations;
	
	public ResourceAllocator(String providerName)
	{
		this.nodes = new HashMap<NodeID, NodeInfo>();
		this.index = new ResourceIndex();
		this.capacity = new MutableResourceSpec();
		this.available = new MutableResourceSpec();
		this.provider = providerName;
		this.numAllocations = 0;
	}
	
	
	/* ========== IResourceAllocator Implementation ========== */
	
	@Override
	public boolean registerNode(Node node)
	{
		if (node == null)
			throw new IllegalArgumentException();

		// Is this node already registered?
		NodeInfo info = nodes.get(node.getId());
		if (info != null)
			return false;

		// Register the new node
		info = new NodeInfo(node);
		capacity.add(node.getCapacity());
		available.add(node.getCapacity());
		nodes.put(node.getId(), info);
		index.add(info);

		return true;
	}

	@Override
	public void unregisterNode(NodeID nodeid)
	{
		if (nodeid == null)
			throw new IllegalArgumentException();

		// Was this node registered?
		NodeInfo node = nodes.remove(nodeid);
		if (node == null)
			return;  // No!

		index.remove(node);

		// All node's resources are gone!
		capacity.sub(node.getCapacity(), true);
		available.sub(node.getCapacity(), true);
		numAllocations -= node.getNumAllocations();
	}

	@Override
	public ResourceSpec getFreeResources()
	{
		return available;
	}

	@Override
	public ResourceSpec getUsedResources()
	{
		return MutableResourceSpec.fromDiff(capacity, available);
	}

	@Override
	public int getNumAllocations()
	{
		return numAllocations;
	}

	@Override
	public ResourceHandle allocate(UUID allocationId, ResourceSpec spec)
	{
		// Are enough free resources available?
		if (!available.reserve(spec))
			return null;  // No!
		
		// Find a node with enough free resources...
		NodeInfo node = index.find(spec, true);
		if (node == null) {
			available.free(spec);  // No node found, release already reserved resources!
			throw new IllegalStateException("ResourceAllocator's ResourceIndex seems to be broken!");
		}

		// Perform the allocation
		ResourceHandle handle = new ResourceHandle(provider, node.getNodeID(), allocationId);
		node.addAllocation(new ResourceAllocation(handle, spec));
		++numAllocations;

		// Re-add the modified node to the index
		index.add(node);
		
		return handle;
	}

	@Override
	public void release(ResourceHandle handle)
	{
		NodeInfo node = nodes.get(handle.getNodeID());
		if (node == null)
			return;

		// Update index
		index.remove(node);
		
		// Update node
		ResourceAllocation allocation = node.removeAllocation(handle);
		if (allocation != null) {
			available.free(allocation.getResourceSpec());
			--numAllocations;
		}
		
		// Re-add the modified node to the index
		index.add(node);
	}
	
	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		
		trigger.setSubResourceDumpHandler(() -> {
			final String segment = dconf.nextUrlSegment();
			switch (segment)
			{
				case "nodes":
					if (dconf.hasMoreUrlSegments()) {
						// Dump specific node...
						final String nid = dconf.nextUrlSegment();
						final NodeInfo node = nodes.get(new NodeID(nid));
						if (node == null)
							throw new NotFoundException("Node '" + nid + "' was not found!");
						
						node.dump(json, dconf, flags);
					}
					else {
						// Dump all nodes...
						final int subflags = DumpFlags.reset(flags, DumpFlags.INLINED);
						json.writeStartArray();
						for (NodeInfo node : nodes.values())
							node.dump(json, dconf, subflags);
	
						json.writeEnd();
					}
					
					break;

				case "index":
					index.dump(json, dconf, flags);
					break;

				default:
					DumpHelpers.notfound(segment);
			}
		});
		
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.CAPACITY, () -> {
				json.write(DumpFields.CAPACITY, DumpHelpers.toJsonObject(capacity));
			});
			
			dumper.add(DumpFields.FREE_RESOURCES, () -> {
				json.write(DumpFields.FREE_RESOURCES, DumpHelpers.toJsonObject(this.getFreeResources()));
			});
			
			dumper.add(DumpFields.USED_RESOURCES, () -> {
				json.write(DumpFields.USED_RESOURCES, DumpHelpers.toJsonObject(this.getUsedResources()));
			});
			
			dumper.add(DumpFields.NUM_ALLOCATIONS, () -> {
				json.write(DumpFields.NUM_ALLOCATIONS, numAllocations);
			});
			
			dumper.add(DumpFields.NODES, () -> {
				final int subflags = DumpFlags.reset(flags, DumpFlags.INLINED);
				json.write("num_" + DumpFields.NODES, nodes.size());
				json.writeStartArray(DumpFields.NODES);
				for (NodeInfo node : nodes.values())
					node.dump(json, dconf, subflags);
				
				json.writeEnd();
			});
			
			dumper.add(DumpFields.INDEX, () -> {
				json.writeStartObject(DumpFields.INDEX);
				index.dump(json, dconf, flags | DumpFlags.INLINED);
				json.writeEnd();
			});
			
			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String NUM_ALLOCATIONS  = "num_allocations";
		private static final String FREE_RESOURCES   = "free_resources";
		private static final String USED_RESOURCES   = "used_resources";
		private static final String CAPACITY         = "capacity";
		private static final String NODES            = "nodes";
		private static final String INDEX            = "index";
	}
}
