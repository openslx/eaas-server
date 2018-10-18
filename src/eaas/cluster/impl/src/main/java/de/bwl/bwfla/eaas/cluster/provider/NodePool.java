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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.NotFoundException;

import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;


public class NodePool implements IDumpable
{
	private final Map<NodeID, Node> registry;
	private final Map<NodeID, Node> registryUnused;
	private final Collection<Node> nodes;
	private final Collection<Node> nodesUnused;
	private final MutableResourceSpec capacity;
	private final MutableResourceSpec pending;
	private final boolean isHomogeneous;
	
	
	public NodePool(boolean isHomogeneous)
	{
		this.registry = new HashMap<NodeID, Node>();
		this.registryUnused = new HashMap<NodeID, Node>();
		this.nodes = Collections.unmodifiableCollection(registry.values());
		this.nodesUnused = Collections.unmodifiableCollection(registryUnused.values());
		this.capacity = new MutableResourceSpec();
		this.pending = new MutableResourceSpec();
		this.isHomogeneous = isHomogeneous;
	}
	
	public void addPendingResources(ResourceSpec spec)
	{
		this.pending.add(spec);
	}
	
	public void removePendingResources(ResourceSpec spec)
	{
		this.pending.sub(spec, true);
	}
	
	public boolean hasPendingResources()
	{
		return pending.isDefined();
	}
	
	public ResourceSpec getPendingResources()
	{
		return pending;
	}
	
	public void registerNode(Node node)
	{
		final Node other = registry.put(node.getId(), node);
		if (other != null)
			capacity.sub(other.getCapacity());
		
		if (!node.isUsed())
			registryUnused.put(node.getId(), node);
		
		final ResourceSpec nodecap = node.getCapacity();
		pending.sub(nodecap, true);
		capacity.add(nodecap);
	}
	
	public boolean unregisterNode(NodeID nid)
	{
		final Node node = registry.remove(nid);
		registryUnused.remove(nid);
		if (node == null)
			return false;
		
		capacity.sub(node.getCapacity());
		return true;
	}
	
	public void onNodeUsedStateChanged(NodeID nid, boolean used)
	{
		if (used) {
			// unused -> used
			registryUnused.remove(nid);
		}
		else {
			// used -> unused
			final Node node = registry.get(nid);
			if (node == null)
				throw new IllegalStateException("Node '" + nid + "' is not registered correctly!");
			
			registryUnused.put(nid, node);
		}
	}
	
	public Collection<Node> getAllNodes()
	{
		return nodes;
	}
	
	public Collection<Node> getUnusedNodes()
	{
		return nodesUnused;
	}
	
	public int getNumNodes()
	{
		return registry.size();
	}

	public int getNumUsedNodes()
	{
		return  this.getNumNodes() - this.getNumUnusedNodes();
	}
	
	public int getNumUnusedNodes()
	{
		return registryUnused.size();
	}
	
	public int getNumHealthyNodes()
	{
		return this.count((node) -> node.isHealthy());
	}
	
	public int getNumUnhealthyNodes()
	{
		return (this.getNumNodes() - this.getNumHealthyNodes());
	}
	
	public ResourceSpec getCapacity()
	{
		return capacity;
	}
	
	public boolean isHomogeneous()
	{
		return isHomogeneous;
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
						final Node node = registry.get(new NodeID(nid));
						if (node == null)
							throw new NotFoundException("Node '" + nid + "' was not found!");

						node.dump(json, dconf, flags);
					}
					else {
						// Dump all nodes...
						final int subflags = DumpFlags.reset(flags, DumpFlags.INLINED);
						json.writeStartArray();
						nodes.forEach((node) -> node.dump(json, dconf, subflags));
						json.writeEnd();
					}

					break;

				default:
					DumpHelpers.notfound(segment);
			}
		});
		
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.IS_HOMOGENEOUS, () -> {
				json.write(DumpFields.IS_HOMOGENEOUS, isHomogeneous);
			});

			dumper.add(DumpFields.CAPACITY, () -> {
				json.write(DumpFields.CAPACITY, DumpHelpers.toJsonObject(capacity));
			});

			dumper.add(DumpFields.PENDING, () -> {
				json.write(DumpFields.PENDING, DumpHelpers.toJsonObject(pending));
			});

			dumper.add(DumpFields.NUM_USED_NODES, () -> {
				json.write(DumpFields.NUM_USED_NODES, this.getNumUsedNodes());
			});

			dumper.add(DumpFields.NUM_UNUSED_NODES, () -> {
				json.write(DumpFields.NUM_UNUSED_NODES, this.getNumUnusedNodes());
			});

			dumper.add(DumpFields.NUM_HEALTHY_NODES, () -> {
				json.write(DumpFields.NUM_HEALTHY_NODES, this.getNumHealthyNodes());
			});

			dumper.add(DumpFields.NUM_UNHEALTHY_NODES, () -> {
				json.write(DumpFields.NUM_UNHEALTHY_NODES, this.getNumUnhealthyNodes());
			});

			dumper.add(DumpFields.NUM_NODES, () -> {
				json.write(DumpFields.NUM_NODES, this.getNumNodes());
			});

			dumper.add(DumpFields.NODES, () -> {
				final int subflags = DumpFlags.reset(flags, DumpFlags.INLINED);
				json.writeStartArray(DumpFields.NODES);
				for (Node node : nodes)
					node.dump(json, dconf, subflags);

				json.writeEnd();
			});
			
			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String IS_HOMOGENEOUS       = "is_homogeneous";
		private static final String CAPACITY             = "capacity";
		private static final String PENDING              = "pending";
		private static final String NUM_USED_NODES       = "num_used_nodes";
		private static final String NUM_UNUSED_NODES     = "num_unused_nodes";
		private static final String NUM_HEALTHY_NODES    = "num_healthy_nodes";
		private static final String NUM_UNHEALTHY_NODES  = "num_unhealthy_nodes";
		private static final String NUM_NODES            = "num_nodes";
		private static final String NODES                = "nodes";
	}
	
	private int count(Predicate<Node> predicate)
	{
		return (int) this.getAllNodes().stream()
				.filter(predicate)
				.count();
	}
}
