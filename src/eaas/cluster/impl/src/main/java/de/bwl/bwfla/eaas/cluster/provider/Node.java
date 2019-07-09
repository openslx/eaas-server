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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;


public class Node implements Comparable<Node>, IDumpable
{
	protected final NodeID id;
	protected final ResourceSpec capacity;
	private final AtomicBoolean healthy;
	private final AtomicBoolean used;
	private final AtomicLong unusedTimestamp;
	private final long bootTimestamp;
	
	/** Callback for receiving changes of node's used-state */
	private BiConsumer<NodeID, Boolean> onUsedStateChangedCallback;
	
	public Node(NodeID id, ResourceSpec capacity)
	{
		final long curtime = Node.nowms();
		
		this.id = id;
		this.capacity = capacity;
		this.healthy = new AtomicBoolean(true);
		this.used = new AtomicBoolean(false);
		this.unusedTimestamp = new AtomicLong(curtime);
		this.bootTimestamp = curtime;
		this.onUsedStateChangedCallback = null;
	}
	
	public NodeID getId()
	{
		return id;
	}
	
	public String getAddress()
	{
		return id.getNodeAddress();
	}
	
	public ResourceSpec getCapacity()
	{
		return capacity;
	}
	
	public long getUptime()
	{
		return (Node.nowms() - bootTimestamp);
	}
	
	public long getUnusedTime()
	{
		if (used.get())
			return 0L;
		
		// Node is unused...
		return (Node.nowms() - unusedTimestamp.get());
	}
	
	public boolean isHealthy()
	{
		return healthy.get();
	}
	
	public void setHealthy(boolean healthy)
	{
		this.healthy.set(healthy);
	}
	
	public boolean isUsed()
	{
		return used.get();
	}
	
	public void setUsed(boolean newused)
	{
		if (used.getAndSet(newused) == newused)
			return;  // State is unchanged!
		
		if (!newused)
			unusedTimestamp.set(Node.nowms());
		
		if (onUsedStateChangedCallback != null)
			onUsedStateChangedCallback.accept(id, newused);
	}
	
	public void setOnUsedStateChangedCallback(BiConsumer<NodeID, Boolean> callback)
	{
		this.onUsedStateChangedCallback = callback;
	}

	@Override
	public int compareTo(Node other)
	{
		return id.compareTo(other.getId());
	}
	
	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.ID, () -> json.write(DumpFields.ID, id.toString()));
			dumper.add(DumpFields.HEALTHY, () -> json.write(DumpFields.HEALTHY, healthy.get()));
			dumper.add(DumpFields.USED, () -> json.write(DumpFields.USED, used.get()));
			dumper.add(DumpFields.UPTIME, () -> {
				json.write(DumpFields.UPTIME, DumpHelpers.toDurationString(this.getUptime()));
			});

			dumper.add(DumpFields.UNUSED_TIME, () -> {
				json.write(DumpFields.UNUSED_TIME, DumpHelpers.toDurationString(this.getUnusedTime()));
			});

			dumper.add(DumpFields.CAPACITY, () -> {
				json.write(DumpFields.CAPACITY, DumpHelpers.toJsonObject(capacity));
			});

			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String ID           = "id";
		private static final String HEALTHY      = "healthy";
		private static final String USED         = "used";
		private static final String UPTIME       = "uptime";
		private static final String UNUSED_TIME  = "unused_time";
		private static final String CAPACITY     = "capacity";
	}
	
	private static long nowms()
	{
		return System.currentTimeMillis();
	}
}
