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

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfig;


// package-private

/** A helper class for performing health checks on nodes */
class NodeHealthChecker implements Runnable
{
	private final Logger log;
	private final Executor executor;
	private final Collection<NodeInfo> nodes;
	private final NodeAllocatorConfig config;
	private final Consumer<NodeID> onDownCallback;
	private final Consumer<Boolean> onRerunCallback;

	public NodeHealthChecker(
			Collection<NodeInfo> nodes,
			NodeAllocatorConfig config,
			Consumer<NodeID> onDownCallback,
			Consumer<Boolean> onRerunCallback,
			Executor executor,
			Logger log)
	{
		this.log = log;
		this.executor = executor;
		this.nodes = nodes;
		this.config = config;
		this.onDownCallback = onDownCallback;
		this.onRerunCallback = onRerunCallback;
	}

	@Override
	public void run()
	{
		// Nothing to check?
		if (nodes.isEmpty()) {
			onRerunCallback.accept(true);
			return;  // Retry later
		}

		// Prepare the health checking subtasks...
		final Queue<Callable<Integer>> healthchecks = new ConcurrentLinkedQueue<Callable<Integer>>();
		for (NodeInfo info : nodes)
			healthchecks.add(this.newHealthCheckTask(info, onDownCallback));

		final int numHealthChecks = nodes.size();
		final int numParallelHealthChecks = Math.min(numHealthChecks, config.getNumParallelHealthChecks());
		final AtomicInteger numRunningWorkers = new AtomicInteger(numParallelHealthChecks);
		final AtomicInteger numTotalFailedNodes = new AtomicInteger(0);
		final AtomicInteger numTotalHealthyNodes = new AtomicInteger(0);

		// Subtask for executing health checks
		final Runnable subtask = () -> {
			Callable<Integer> healthcheck = null;
			int numFailedNodes = 0, numHealthyNodes = 0;
			while ((healthcheck = healthchecks.poll()) != null) {
				try {
					final int result = healthcheck.call();
					if (result < 0)
						++numFailedNodes;
					else if (result > 0)
						++numHealthyNodes;
				}
				catch (Exception exception) {
					log.log(Level.WARNING, "Executing health check failed!", exception);
				}
			}

			numTotalFailedNodes.addAndGet(numFailedNodes);
			numTotalHealthyNodes.addAndGet(numHealthyNodes);

			// Last subtask re-schedules health checks
			if (numRunningWorkers.decrementAndGet() != 0)
				return;

			onRerunCallback.accept(true);

			// Print summary
			numFailedNodes = numTotalFailedNodes.get();
			numHealthyNodes = numTotalHealthyNodes.get();
			final String message = new StringBuilder(256)
					.append("Health checking summary: ")
					.append(numHealthyNodes)
					.append(" out of ")
					.append(numHealthChecks)
					.append(" node(s) healthy, ")
					.append(numHealthChecks - numHealthyNodes - numFailedNodes)
					.append(" unhealthy, ")
					.append(numFailedNodes)
					.append(" failed")
					.toString();

			log.info(message);
		};

		// Submit the subtasks for parallel processing
		for (int i = 0; i < numParallelHealthChecks; ++i)
			executor.execute(subtask);
	}

	private Callable<Integer> newHealthCheckTask(NodeInfo info, Consumer<NodeID> onDownCallback)
	{
		final Callable<Integer> healthcheck = () -> {
			final NodeState prevState = info.getNodeState();
			NodeState state = NodeHealthCheck.run(info, config, log);
			if (state != prevState) {
				final String message = new StringBuilder(512)
						.append("State of node '")
						.append(info.getNodeId().toString())
						.append("' changed: ")
						.append(prevState.toString())
						.append(" -> ")
						.append(state.toString())
						.toString();

				log.info(message);
			}

			if (state == NodeState.FAILED) {
				// Node failed and should be removed!
				final NodeID nid = info.getNodeId();
				log.warning("Node '" + nid.toString() + "' is down!");
				onDownCallback.accept(nid);
				return -1;
			}

			return (info.getNode().isHealthy()) ? 1 : 0;
		};

		return healthcheck;
	}
}
