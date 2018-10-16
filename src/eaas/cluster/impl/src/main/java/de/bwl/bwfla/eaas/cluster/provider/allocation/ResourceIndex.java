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

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;

// package-private

/** An index for nodes, sorted by node's free resources. */
class ResourceIndex implements IDumpable
{
	private final NavigableMap<Long, HashSet<NodeInfo>> entries;

	private static final long CPU_SHIFT   = 32L;
	private static final long MEMORY_MASK = 0xFFFFFFFFL;

	private static final Comparator<Long> ENTRIES_COMPARATOR = (k1, k2) -> {
		final int result = Long.compare(k1 >> CPU_SHIFT, k2 >> CPU_SHIFT);
		if (result != 0)
			return result;

		return Long.compare(k1 & MEMORY_MASK, k2 & MEMORY_MASK);
	};
	
	public ResourceIndex()
	{
		this.entries = new TreeMap<Long, HashSet<NodeInfo>>(ENTRIES_COMPARATOR);
	}
	
	public void add(NodeInfo node)
	{
		final long key = ResourceIndex.toKey(node.getFreeResources());
		HashSet<NodeInfo> nodes = entries.get(key);
		if (nodes == null) {
			nodes = new LinkedHashSet<NodeInfo>();
			entries.put(key, nodes);
		}

		nodes.add(node);
	}
	
	public boolean remove(NodeInfo node)
	{
		final long key = ResourceIndex.toKey(node.getFreeResources());
		return this.remove(key, node);
	}

	/** Find a node with enough free resources */
	public NodeInfo find(ResourceSpec spec, boolean remove)
	{
		Map.Entry<Long, HashSet<NodeInfo>> entry =
				entries.ceilingEntry(ResourceIndex.toKey(spec));
		
		while (entry != null) {
			final HashSet<NodeInfo> nodes = entry.getValue();
			final NodeInfo node = this.findHealthyNode(nodes);
			if (node != null) {
				if (remove)
					this.remove(entry.getKey(), node);
				
				return node;
			}
			
			// There is no healthy node found for current entry,
			// try to find one in the next entry!
			entry = entries.higherEntry(entry.getKey());
		}
		
		// No suitable node could be found!
		return null;
	}
	
	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final BiConsumer<Long, HashSet<NodeInfo>> entryDumpFunctor = (id, nodes) -> {
				json.writeStartObject();
				json.write("key", id);
				json.write("num_nodes", nodes.size());
				json.writeStartArray("nodes");
				
				nodes.forEach((node) -> json.write(node.getNodeID().toString()));
				
				json.writeEnd();
				json.writeEnd();
			};
			
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add("entries", () -> {
				json.write("num_entries", entries.size());
				json.writeStartArray("entries");
				entries.forEach(entryDumpFunctor);
				json.writeEnd();
			});
			
			dumper.run();
		});
		
		trigger.run();
	}
	
	private NodeInfo findHealthyNode(HashSet<NodeInfo> nodes)
	{
		for (NodeInfo node : nodes) {
			if (node.isHealthy())
				return node;
		}
		
		return null;
	}
	
	private boolean remove(long key, NodeInfo node)
	{
		final HashSet<NodeInfo> nodes = entries.get(key);
		if (nodes == null)
			return false;

		final boolean removed = nodes.remove(node);
		if (nodes.isEmpty()) {
			// Clean up empty entry
			entries.remove(key);
		}

		return removed;
	}

	private static long toKey(ResourceSpec spec)
	{
		return ((long) spec.cpu()) << CPU_SHIFT | (long) spec.memory();
	}
}