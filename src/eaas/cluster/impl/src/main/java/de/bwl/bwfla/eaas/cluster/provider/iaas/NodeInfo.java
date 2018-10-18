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

package de.bwl.bwfla.eaas.cluster.provider.iaas;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;
import de.bwl.bwfla.eaas.cluster.provider.Node;

// package-private

/** A simple class containing runtime information about a node */
class NodeInfo implements Comparable<NodeInfo>, IDumpable
{
	private final Node node;
	private NodeState state;
	private final URL healthCheckUrl;
	private long unreachableTimestamp;
	private final Map<String, Object> metadata;
	
	public NodeInfo(Node node, String healthCheckUrl) throws MalformedURLException
	{
		this(node, new URL(healthCheckUrl));
	}
	
	public NodeInfo(Node node, URL healthCheckUrl)
	{
		this.node = node;
		this.state = NodeState.UNKNOWN;
		this.healthCheckUrl = healthCheckUrl;
		this.unreachableTimestamp = Long.MAX_VALUE;
		this.metadata = new HashMap<String, Object>();
	}
	
	public Node getNode()
	{
		return node;
	}
	
	public NodeID getNodeId()
	{
		return node.getId();
	}
	
	public NodeState getNodeState()
	{
		return state;
	}
	
	public void setNodeState(NodeState newState)
	{
		this.state = newState;
	}
	
	public URL getHealthCheckUrl()
	{
		return healthCheckUrl;
	}
	
	public long getUnreachableTimestamp()
	{
		return unreachableTimestamp;
	}
	
	public void setUnreachableTimestamp(long timestamp)
	{
		this.unreachableTimestamp = timestamp;
	}
	
	public void resetUnreachableTimestamp()
	{
		unreachableTimestamp = Long.MAX_VALUE;
	}
	
	public Map<String, Object> getMetadata()
	{
		return metadata;
	}

	@Override
	public int compareTo(NodeInfo other)
	{
		return node.compareTo(other.getNode());
	}

	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.ID, () -> json.write(DumpFields.ID, node.getId().toString()));
			dumper.add(DumpFields.STATE, () -> json.write(DumpFields.STATE, state.toString()));
			dumper.add(DumpFields.HEALTHCHECK_URL, () -> {
				json.write(DumpFields.HEALTHCHECK_URL, healthCheckUrl.toString());
			});
	
			dumper.add(DumpFields.METADATA, () -> {
				final BiConsumer<String, Object> functor = (key, value) -> {
					json.writeStartObject()
						.write(key, value.toString())
						.writeEnd();
				};
				
				json.writeStartArray(DumpFields.METADATA);
				metadata.forEach(functor);
				json.writeEnd();
			});
			
			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String ID               = "id";
		private static final String STATE            = "state";
		private static final String HEALTHCHECK_URL  = "healthcheck_url";
		private static final String METADATA         = "metadata";
	}
}
