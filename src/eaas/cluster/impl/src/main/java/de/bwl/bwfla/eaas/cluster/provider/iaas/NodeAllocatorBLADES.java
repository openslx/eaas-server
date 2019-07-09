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
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.common.concurrent.SequentialExecutor;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;
import de.bwl.bwfla.eaas.cluster.ClusterManagerExecutors;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceDiff;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfigBLADES;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;
import de.bwl.bwfla.eaas.cluster.provider.Node;
import de.bwl.bwfla.eaas.cluster.provider.NodeAllocationRequest;


public class NodeAllocatorBLADES implements INodeAllocator
{
	public static final String VAR_ADDRESS = "{{address}}";

	protected final Logger log;
	
	protected final NodeAllocatorConfigBLADES config;
	protected final Map<NodeID, NodeInfo> usedNodes;
	protected final NavigableSet<NodeInfo> freeNodes;
	protected final NavigableSet<NodeInfo> failedNodes;
	protected final ResourceSpec nodeCapacity;
	protected final Consumer<NodeID> onDownCallback;
	protected final ClusterManagerExecutors executors;
	protected final SequentialExecutor tasks;
	
	
	public NodeAllocatorBLADES(String protocol, NodeAllocatorConfigBLADES config, Consumer<NodeID> onDownCallback,
			ClusterManagerExecutors executors, PrefixLoggerContext parentLogContext) throws MalformedURLException
	{
		final PrefixLoggerContext logContext = new PrefixLoggerContext(parentLogContext)
				.add("NA", "blades");
		
		this.log = new PrefixLogger(this.getClass().getName(), logContext);
		this.config = config;
		this.usedNodes = new HashMap<NodeID, NodeInfo>();
		this.freeNodes = new TreeSet<NodeInfo>();
		this.failedNodes = new TreeSet<NodeInfo>();
		this.nodeCapacity = config.getNodeCapacity();
		this.onDownCallback = onDownCallback;
		this.executors = executors;
		this.tasks = new SequentialExecutor(log, executors.computation(), 64);
		
		final String urlTemplate = config.getHealthCheckUrl();
		
		for (String address : config.getNodeAddresses()) {
			final NodeID id = new NodeID(address).setProtocol(protocol);
			final Node node = new Node(id, nodeCapacity);
			String healthCheckUrl = urlTemplate.replace(VAR_ADDRESS, node.getAddress());
			freeNodes.add(new NodeInfo(node, healthCheckUrl));
		}
		
		this.scheduleHealthChecking(false);
	}
	
	
	/* ========== INodeAllocator Implementation ========== */
	
	@Override
	public ResourceSpec getNodeCapacity()
	{
		return nodeCapacity;
	}
	
	@Override
	public ResourceSpec allocate(NodeAllocationRequest request)
	{
		if (request.getSpec() == null)
			throw new IllegalArgumentException("No ResourceSpec specified!");
	
		if (request.getOnUpCallback() == null || request.getOnErrorCallback() == null)
			throw new IllegalArgumentException("No callbacks specified!");
		
		// Compute the number of nodes to start...
		final int numRequestedNodes = NodeAllocatorUtil.computeNumRequiredNodes(request.getSpec(), nodeCapacity);
		final ResourceSpec pending = ResourceSpec.create(numRequestedNodes, nodeCapacity);

		Runnable task = () -> {
			final ResourceDiff missing = new ResourceDiff(pending);
			for (int i = numRequestedNodes; i > 0; --i) {
				// Allocate a new node
				NodeInfo info = freeNodes.pollFirst();
				if (info == null) {
					// No free nodes found!
					final int cpu = Math.max(0, missing.cpu());
					final int memory = Math.max(0, missing.memory());
					final ResourceSpec failedSpec =
							ResourceSpec.create(cpu, ResourceSpec.CPU_UNIT, memory, ResourceSpec.MEMORY_UNIT);
					
					request.getOnErrorCallback().accept(failedSpec);
					break;
				}
	
				// Node found!
				Node node = info.getNode();
				usedNodes.put(node.getId(), info);
				request.getOnUpCallback().accept(node);
	
				missing.subtract(nodeCapacity);
			}
		};
		
		this.submit(task);
		return pending;
	}

	@Override
	public CompletableFuture<Boolean> release(NodeID nid)
	{
		if (nid == null)
			throw new IllegalArgumentException("Invalid node ID specified!");
		
		Runnable task = () -> {
			NodeInfo info = usedNodes.remove(nid);
			if (info == null)
				return;
			
			freeNodes.add(info);
		};
		
		this.submit(task);
		return CompletableFuture.completedFuture(true);
	}
	
	@Override
	public boolean terminate()
	{
		return true;
	}
	
	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		
		trigger.setSubResourceDumpHandler(() -> {
			final String segment = dconf.nextUrlSegment();
			switch (segment)
			{
				case "config":
					config.dump(json, dconf, flags);
					break;

				case "used-nodes":
					json.writeStartArray();
					for (NodeInfo node : usedNodes.values())
						node.dump(json, dconf, flags);

					json.writeEnd();
					break;

				case "free-nodes":
					json.writeStartArray();
					for (NodeInfo node : freeNodes)
						node.dump(json, dconf, flags);

					json.writeEnd();
					break;

				case "failed-nodes":
					json.writeStartArray();
					for (NodeInfo node : failedNodes)
						node.dump(json, dconf, flags);

					json.writeEnd();
					break;

				default:
					DumpHelpers.notfound(segment);
			}
		});
		
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			
			dumper.add(DumpFields.CONFIG, () -> {
				json.writeStartObject(DumpFields.CONFIG);
				config.dump(json, dconf, flags | DumpFlags.INLINED);
				json.writeEnd();
			});

			dumper.add(DumpFields.NODE_CAPACITY, () -> {
				json.write(DumpFields.NODE_CAPACITY, DumpHelpers.toJsonObject(nodeCapacity));
			});

			final int subflags = DumpFlags.reset(flags, DumpFlags.INLINED);

			dumper.add(DumpFields.USED_NODES, () -> {
				json.write("num_" + DumpFields.USED_NODES, usedNodes.size());
				json.writeStartArray(DumpFields.USED_NODES);
				for (NodeInfo node : usedNodes.values())
					node.dump(json, dconf, subflags);

				json.writeEnd();
			});

			dumper.add(DumpFields.FREE_NODES, () -> {
				json.write("num_" + DumpFields.FREE_NODES, freeNodes.size());
				json.writeStartArray(DumpFields.FREE_NODES);
				for (NodeInfo node : freeNodes)
					node.dump(json, dconf, subflags);

				json.writeEnd();
			});

			dumper.add(DumpFields.FAILED_NODES, () -> {
				json.write("num_" + DumpFields.FAILED_NODES, failedNodes.size());
				json.writeStartArray(DumpFields.FAILED_NODES);
				for (NodeInfo node : failedNodes)
					node.dump(json, dconf, subflags);

				json.writeEnd();
			});
			
			dumper.run();
		});
		
		try {
			tasks.submit(trigger).get();
		}
		catch (Exception exception) {
			log.log(Level.WARNING, "Dumping internal state failed!", exception);
		}
	}
	
	private static class DumpFields
	{
		private static final String CONFIG         = "config";
		private static final String NODE_CAPACITY  = "node_capacity";
		private static final String USED_NODES     = "used_nodes";
		private static final String FREE_NODES     = "free_nodes";
		private static final String FAILED_NODES   = "failed_nodes";
	}

	
	/* ==================== Internal Helpers ==================== */
	
	private void submit(Runnable task)
	{
		tasks.execute(task);
	}
	
	private Runnable newHealthCheckTask(NodeInfo info, Consumer<NodeID> onDownCallback)
	{
		Runnable healthcheck = () -> {
			NodeState state = NodeHealthCheck.run(info, config, log);
			if (state != NodeState.FAILED)
				return;
			
			// Node failed and should be removed!
			if (onDownCallback != null)
				onDownCallback.accept(info.getNodeId());
			
			// Update internal state...
			Runnable task = () -> {
				usedNodes.remove(info.getNodeId());
				freeNodes.remove(info);
				failedNodes.add(info);
			};
			
			this.submit(task);
		};
		
		return healthcheck;
	}
	
	private void scheduleHealthChecking(boolean delayed)
	{
		// Action to execute...
		Runnable task = () -> {
			final Queue<Runnable> healthchecks = new ConcurrentLinkedQueue<Runnable>();
			int numHealthChecks = 0;
			
			// Prepare the health checking subtasks...
			
			for (NodeInfo info : usedNodes.values()) {
				healthchecks.add(this.newHealthCheckTask(info, onDownCallback));
				++numHealthChecks;
			}
			
			for (NodeInfo info : freeNodes) {
				healthchecks.add(this.newHealthCheckTask(info, null));
				++numHealthChecks;
			}
			
			for (NodeInfo info : failedNodes) {
				Runnable healthcheck = () -> {
					NodeState state = NodeHealthCheck.run(info, config, log);
					if (state != NodeState.REACHABLE)
						return;
					
					// Node is now reachable
					Runnable subtask = () -> {
						failedNodes.remove(info);
						freeNodes.add(info);
					};
					
					this.submit(subtask);
				};
				
				healthchecks.add(healthcheck);
				++numHealthChecks;
			}
			
			final int numParallelHealthChecks = Math.min(numHealthChecks, config.getNumParallelHealthChecks());
			final AtomicInteger counter = new AtomicInteger(numParallelHealthChecks);
			
			// Subtask for executing health checks
			Runnable subtask = () -> {
				Runnable healthcheck = null;
				while ((healthcheck = healthchecks.poll()) != null) {
					try {
						healthcheck.run();
					}
					catch (Exception exception) {
						log.log(Level.SEVERE, exception.getMessage(), exception);
					}
				}
				
				// Last subtask re-schedules health checks
				if (counter.decrementAndGet() == 0)
					this.scheduleHealthChecking(true);
			};
			
			// Submit the subtasks for parallel processing
			for (int i = 0; i < numParallelHealthChecks; ++i)
				executors.io().execute(subtask);
		};
		
		// Action trigger...
		Runnable trigger = () -> {
			this.submit(task);
		};

		if (delayed) {
			// Submit the action-trigger
			final long delay = config.getHealthCheckInterval();
			executors.scheduler().schedule(trigger, delay, TimeUnit.MILLISECONDS);
		}
		else {
			// Execute now!
			trigger.run();
		}
	}
}
