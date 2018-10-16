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

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfig;

// package-private

/** A task for polling the VM boot process. */
class NodeReachabilityPollTask implements Runnable
{
	private final Logger log;
	private final Executor executor;
	private final ScheduledExecutorService scheduler;
	private final CompletableFuture<NodeInfo> result;
	private final NodeAllocatorConfig config;
	private final NodeInfo info;
	private Random random;
	private final long minPollInterval;
	private final int maxPollIntervalDelta;
	private final int maxNumRetries;
	private int numRetries;

	private NodeReachabilityPollTask(Builder builder)
	{
		this.log = builder.log;
		this.executor = builder.executor;
		this.scheduler = builder.scheduler;
		this.result = new CompletableFuture<NodeInfo>();
		this.config = builder.config;
		this.info = builder.info;
		this.random = null;
		this.minPollInterval = builder.minPollInterval;
		this.maxPollIntervalDelta = builder.maxPollIntervalDelta;
		this.maxNumRetries = builder.maxNumRetries;
		this.numRetries = 0;
	}

	public CompletableFuture<NodeInfo> completion()
	{
		return result;
	}

	@Override
	public void run()
	{
		try {
			final NodeState state = NodeHealthCheck.run(info, config, true);
			if (state == NodeState.REACHABLE || state == NodeState.FAILED) {
				result.complete(info);
				return;
			}

			// Node is not yet reachable, retry later
			final long delay = this.nextPollDelay();
			if (delay < 0L) {
				final String message = "Max. number of reachability checks ran!"
						+ " Aborting after " + numRetries + " retries...";

				log.warning(message);
				result.complete(info);
				return;
			}

			final Runnable trigger = () -> executor.execute(this);
			scheduler.schedule(trigger, delay, TimeUnit.MILLISECONDS);
		}
		catch (Throwable error) {
			log.log(Level.WARNING, "Checking node reachability failed!\n", error);
			result.complete(info);
		}
	}

	private long nextPollDelay()
	{
		if (numRetries >= maxNumRetries)
			return -1;

		++numRetries;

		if (maxPollIntervalDelta == 0)
			return minPollInterval;

		if (random == null)
			random = new Random();

		int delta = random.nextInt(maxPollIntervalDelta);
		return (minPollInterval + (long) delta);
	}


	public static class Builder
	{
		private Logger log;
		private ScheduledExecutorService scheduler;
		private Executor executor;
		private NodeAllocatorConfig config;
		private NodeInfo info;
		private long minPollInterval;
		private int maxPollIntervalDelta;
		private int maxNumRetries;

		public Builder()
		{
			this.log = null;
			this.scheduler = null;
			this.executor = null;
			this.config = null;
			this.info = null;
			this.minPollInterval = TimeUnit.SECONDS.toMillis(0L);
			this.maxPollIntervalDelta = (int) TimeUnit.SECONDS.toMillis(1L);
			this.maxNumRetries = 3;
		}

		public Builder setLogger(Logger log)
		{
			this.log = log;
			return this;
		}

		public Builder setScheduler(ScheduledExecutorService scheduler)
		{
			this.scheduler = scheduler;
			return this;
		}

		public Builder setExecutor(Executor executor)
		{
			this.executor = executor;
			return this;
		}

		public Builder setNodeAllocatorConfig(NodeAllocatorConfig config)
		{
			this.config = config;
			return this;
		}

		public Builder setNodeInfo(NodeInfo info)
		{
			this.info = info;
			return this;
		}

		public Builder setPollInterval(long interval, TimeUnit unit)
		{
			this.minPollInterval = unit.toMillis(interval);
			return this;
		}

		public Builder setPollInterval(long min, long max, TimeUnit unit)
		{
			this.minPollInterval = unit.toMillis(min);
			this.maxPollIntervalDelta = (int) (unit.toMillis(max) - this.minPollInterval);
			return this;
		}

		public Builder setPollIntervalDelta(long delta, TimeUnit unit)
		{
			this.maxPollIntervalDelta = (int) unit.toMillis(delta);
			return this;
		}

		public Builder setMaxNumRetries(int number)
		{
			this.maxNumRetries = number;
			return this;
		}

		public Builder setUnlimitedNumRetries()
		{
			this.maxNumRetries = Integer.MAX_VALUE;
			return this;
		}

		public NodeReachabilityPollTask build()
		{
			if (log == null)
				log = Logger.getLogger(NodeReachabilityPollTask.class.getName());

			if (scheduler == null)
				throw new IllegalStateException("Scheduler is missing!");

			if (executor == null)
				throw new IllegalStateException("Executor is missing!");

			if (config == null)
				throw new IllegalStateException("Config is missing!");

			if (info == null)
				throw new IllegalStateException("Node info is missing!");

			if (minPollInterval < 0L)
				throw new IllegalStateException("Min. poll interval is negative!");

			if (maxPollIntervalDelta < 0)
				throw new IllegalStateException("Max. poll interval delta is negative!");

			if (maxNumRetries < 0)
				throw new IllegalStateException("Max. number of retries is negative!");

			return new NodeReachabilityPollTask(this);
		}
	}
}
