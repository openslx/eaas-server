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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.common.concurrent.SequentialExecutor;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.conf.CommonSingleton;
import de.bwl.bwfla.eaas.cluster.ClusterManagerExecutors;
import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfigBLADES;
import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfigGCE;
import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfigJCLOUDS;
import de.bwl.bwfla.eaas.cluster.config.NodePoolScalerConfig;
import de.bwl.bwfla.eaas.cluster.config.HomogeneousNodePoolScalerConfig;
import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfig;
import de.bwl.bwfla.eaas.cluster.config.ResourceProviderConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;
import de.bwl.bwfla.eaas.cluster.exception.OutOfResourcesException;
import de.bwl.bwfla.eaas.cluster.metadata.LabelSelector;
import de.bwl.bwfla.eaas.cluster.metadata.LabelIndex;
import de.bwl.bwfla.eaas.cluster.provider.allocation.IResourceAllocator;
import de.bwl.bwfla.eaas.cluster.provider.allocation.ResourceAllocator;
import de.bwl.bwfla.eaas.cluster.provider.iaas.INodeAllocator;
import de.bwl.bwfla.eaas.cluster.provider.iaas.NodeAllocatorBLADES;
import de.bwl.bwfla.eaas.cluster.provider.iaas.NodeAllocatorGCE;
import de.bwl.bwfla.eaas.cluster.provider.iaas.NodeAllocatorJCLOUDS;
import de.bwl.bwfla.eaas.cluster.provider.iaas.NodeNameGenerator;
import de.bwl.bwfla.eaas.cluster.rest.NodePoolDescription;
import de.bwl.bwfla.eaas.cluster.rest.ResourceProviderDescription;


public class ResourceProvider implements IResourceProvider
{
	private static final int PRIORITY_DEFAULT   = SequentialExecutor.getDefaultPriority();
	private static final int PRIORITY_ALLOCATE  = PRIORITY_DEFAULT + 10;
	private static final int PRIORITY_RELEASE   = PRIORITY_DEFAULT + 11;
	private static final int PRIORITY_NODEMGMNT = PRIORITY_DEFAULT + 20;

	private static final int DNS_TRANSACTION_RETRIES_NUM = 5;

	private final static String DNS_REGISTER_SCRIPT = "/libexec/register-dns";
	private final static String DNS_UNREGISTER_SCRIPT = "/libexec/unregister-dns";
	private final static String CLOUD_CONFIG_SCRIPT = "/libexec/get-cloud-config";

	private final PrefixLogger log;

	private final NodeNameGenerator nodeNameGenerator;
	private final ResourceProviderMetrics metrics;
	private final ResourceProviderConfig config;
	private final ScheduledExecutorService scheduler;
	private final SequentialExecutor executor;
	private final IResourceAllocator resources;
	private final INodeAllocator nodes;
	private final NodePoolScaler poolscaler;
	private final NodePool pool;
	private final LabelIndex labels;

	private final Executor ioExecutor;

	private final AllocationRequestHistory history;
	private final AllocationRequestQueue requests;

	private boolean isOneShotPoolScalingScheduled;
	private boolean isShutdownRequested;
	
	
	public ResourceProvider(ResourceProviderConfig config, ClusterManagerExecutors executors) throws Exception
	{
		final PrefixLoggerContext logContext = new PrefixLoggerContext()
				.add("RP", config.getName());
		
		this.log = new PrefixLogger(this.getClass().getName(), logContext);
		this.nodeNameGenerator = new NodeNameGenerator("ec-");
		this.metrics = new ResourceProviderMetrics();
		this.config = config;
		this.scheduler = executors.scheduler();
		this.executor = new SequentialExecutor(log, executors.computation(), 128);
		this.resources = new ResourceAllocator(config.getName());
		this.pool = new NodePool(config.hasHomogeneousNodes());
		this.labels = new LabelIndex();
		this.history = new AllocationRequestHistory(config.getRequestHistoryMaxNumRequests(), config.getRequestHistoryMaxRequestAge());
		this.requests = new AllocationRequestQueue();
		this.isOneShotPoolScalingScheduled = false;
		this.isShutdownRequested = false;
		this.ioExecutor = executors.io();
		
		Consumer<NodeID> onDownCallback = (NodeID nid) -> {
			// Task for unregistering the node from this provider
			Runnable task = () -> {
				this.unregisterNode(nid);
			};
			
			this.submit(PRIORITY_NODEMGMNT, task);
		};
		
		// Construct the requested node allocator for the provider
		final NodeAllocatorConfig nodeConfig = config.getNodeAllocatorConfig();
		final NodePoolScalerConfig poolScalerConfig = config.getPoolScalerConfig();
		if (nodeConfig.getClass() == NodeAllocatorConfigBLADES.class) {
			this.nodes = new NodeAllocatorBLADES(config.getProtocol(), (NodeAllocatorConfigBLADES) nodeConfig, onDownCallback, executors, logContext);
			this.poolscaler = NodePoolScaler.create((HomogeneousNodePoolScalerConfig) poolScalerConfig, nodes.getNodeCapacity());
		}
		else if (nodeConfig.getClass() == NodeAllocatorConfigGCE.class) {
			this.nodes = new NodeAllocatorGCE((NodeAllocatorConfigGCE) nodeConfig, onDownCallback, executors, logContext);
			this.poolscaler = NodePoolScaler.create((HomogeneousNodePoolScalerConfig) poolScalerConfig, nodes.getNodeCapacity());
		}
		else if (nodeConfig.getClass() == NodeAllocatorConfigJCLOUDS.class) {
			this.nodes = new NodeAllocatorJCLOUDS((NodeAllocatorConfigJCLOUDS) nodeConfig, onDownCallback, executors, logContext);
			this.poolscaler = NodePoolScaler.create((HomogeneousNodePoolScalerConfig) poolScalerConfig, nodes.getNodeCapacity());
		}
		else {
			final String clazz = nodeConfig.getClass().getName();
			throw new IllegalArgumentException("Invalid or unsupported NodeAllocator configured: " + clazz);
		}
		
		// Build the label index
		config.getLabels().forEach((label) -> labels.add(label));
		
		// Schedule background tasks
		this.schedulePoolScaling(false, false);
		this.scheduleRequestHistoryUpdate(true, false);
		this.scheduleDeferredAllocationsGc(true, false);
	}
	
	@Override
	public CompletableFuture<ResourceHandle> allocate(UUID allocationId, ResourceSpec spec, boolean scaleup, long timeout, TimeUnit unit)
	{
		if (allocationId == null || spec == null)
			throw new IllegalArgumentException();

		CompletableFuture<ResourceHandle> result = new CompletableFuture<ResourceHandle>();
		final long deadline = ResourceProvider.getCurrentTime() + unit.toMillis(timeout);

		Runnable task = () -> {
			try {
				// Update stats
				metrics.requested();
				history.add(spec);
	
				// Check request's deadline
				if (ResourceProvider.isDeadlineExpired(deadline)) {
					log.warning("Deadline expired for allocation " + allocationId + "! Aborting...");
					result.cancel(false);
					metrics.expired();
					return;
				}
				
				// Try to allocate requested resources
				ResourceHandle handle = resources.allocate(allocationId, spec);
				if (handle != null) {
					log.info("Resources for " + allocationId + " allocated on node '" + handle.getNodeID() + "'");
					result.complete(handle);
					return;
				}

				// Can we defere this allocation request?
				// Maybe yes, if the node pool has not reached its max. size!
				if (scaleup && !this.isMaxPoolSizeReached()) {
					log.info("Defere allocation " + allocationId + " until the pool is scaled up!");
					requests.add(new AllocationRequest(result, allocationId, spec, deadline));
					this.schedulePoolScaling(false, true);
					metrics.deferred();
					return;
				}
				
				log.warning("Not enough free resources for allocation " + allocationId + " available! Aborting...");
				result.completeExceptionally(new OutOfResourcesException());
				metrics.failed();
			}
			catch (Throwable throwable) {
				// Something unexpected happened during the allocation
				log.log(Level.WARNING, "Allocation " + allocationId + " failed!\n", throwable);
				result.completeExceptionally(throwable);
				metrics.failed();
			}
		};
		
		// Log the processing time...
		{
			final long startTimestamp = ResourceProvider.getCurrentTime();
			final BiConsumer<ResourceHandle, Throwable> trigger = (handle, error) -> {
				String suffix = "second(s)";
				long duration = ResourceProvider.getCurrentTime() - startTimestamp;
				if (duration > 1000L)
					duration = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS);
				else suffix = "milli" + suffix;
				
				log.info("Processing allocation " + allocationId + " took " + duration + " " + suffix);
			};
			
			result.whenComplete(trigger);
		}
		
		// Check, whether the request can be serviced at all
		if (ResourceSpec.compare(spec, nodes.getNodeCapacity()) <= 0) {
			// Yes, submit the request
			this.submit(PRIORITY_ALLOCATE, task);
		}
		else {
			final String message = "Requested resources can't be serviced by this provider!\n"
					+ "        Requested resources: " + spec + "\n"
					+ "        Node's capacity:     " + nodes.getNodeCapacity();
			
			log.warning(message);
			result.cancel(false);
		}
		
		return result;
	}
	
	@Override
	public CompletableFuture<ResourceSpec> release(ResourceHandle handle)
	{
		if (handle == null)
			throw new IllegalArgumentException();

		CompletableFuture<ResourceSpec> result = new CompletableFuture<>();

		Runnable task = () -> {
			result.complete(resources.release(handle));
			log.info("Resources for allocation " + handle.getAllocationID() + " released");
			log.info("Resources from " + resources.getNumAllocations() + " allocation(s) still reserved");
			this.processDeferredAllocations();
		};
		
		this.submit(PRIORITY_RELEASE, task);
		return result;
	}
	
	@Override
	public CompletableFuture<Boolean> shutdown()
	{
		ShutdownTask task = new ShutdownTask();
		isShutdownRequested = true;
		this.submit(task);
		return task.completion();
	}
	
	@Override
	public boolean terminate()
	{
		return nodes.terminate();
	}
	
	@Override
	public boolean apply(LabelSelector selector)
	{
		return labels.apply(selector);
	}
	
	@Override
	public boolean apply(Collection<LabelSelector> selectors)
	{
		return selectors.stream()
			.allMatch((selector) -> this.apply(selector));
	}
	
	@Override
	public LabelIndex getLabelIndex()
	{
		return labels;
	}
	
	@Override
	public String getName()
	{
		return config.getName();
	}
	
	public Metrics getMetrics()
	{
		return metrics;
	}

	@Override
	public ResourceProviderDescription describe(boolean detailed)
	{
		final Callable<ResourceProviderDescription> trigger = () -> {
			final NodePoolDescription npdesc = new NodePoolDescription()
					.setCapacity(pool.getCapacity())
					.setPendingResources(pool.getPendingResources())
					.setFreeResources(resources.getFreeResources());

			if (detailed) {
				npdesc.setNumNodesUnhealthy(pool.getNumUnhealthyNodes())
						.setNumNodesUnused(pool.getNumUnusedNodes())
						.setNodes(resources.describe(detailed));
			}

			return new ResourceProviderDescription(this.getName(), config.getType())
					.setNumRequestsTotal(metrics.getNumRequests())
					.setNumRequestsDeferred(metrics.getNumRequestsDeferred())
					.setNumRequestsExpired(metrics.getNumRequestsExpired())
					.setNumRequestsFailed(metrics.getNumRequestsFailed())
					.setNodePool(npdesc);
		};

		try {
			return executor.submit(trigger)
					.get();
		}
		catch (Exception exception) {
			log.log(Level.SEVERE,"Describing resource provider failed!", exception);
			return null;
		}
	}

	
	/** Exposed provider's metrics */
	public static interface Metrics
	{
		public int getNumRequests();
		public int getNumRequestsDeferred();
		public int getNumRequestsFailed();
		public int getNumRequestsExpired();
	}
	
	
	/* ========== Debug REST-API ========== */
	
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

				case "metrics":
					metrics.dump(json, dconf, flags);
					break;

				case "node-pool":
					pool.dump(json, dconf, flags);
					break;

				case "node-allocator":
					nodes.dump(json, dconf, flags);
					break;

				case "resource-allocator":
					resources.dump(json, dconf, flags);
					break;
				
				case "allocation-requests":
					requests.dump(json, dconf, flags);
					break;

				default:
					DumpHelpers.notfound(segment);
			}
		});
		
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.NAME, () -> json.write(DumpFields.NAME, config.getName()));
			dumper.add(DumpFields.TYPE, () -> json.write(DumpFields.TYPE, config.getType()));

			final int subflags = DumpFlags.set(flags, DumpFlags.INLINED);

			dumper.add(DumpFields.CONFIG, () -> {
				json.writeStartObject(DumpFields.CONFIG);
				config.dump(json, dconf, subflags);
				json.writeEnd();
			});

			dumper.add(DumpFields.METRICS, () -> {
				json.writeStartObject(DumpFields.METRICS);
				metrics.dump(json, dconf, subflags);
				json.writeEnd();
			});

			dumper.add(DumpFields.NODE_POOL, () -> {
				json.writeStartObject(DumpFields.NODE_POOL);
				pool.dump(json, dconf, subflags);
				json.writeEnd();
			});

			dumper.add(DumpFields.NODE_ALLOCATOR, () -> {
				json.writeStartObject(DumpFields.NODE_ALLOCATOR);
				nodes.dump(json, dconf, subflags);
				json.writeEnd();
			});

			dumper.add(DumpFields.RESOURCE_ALLOCATOR, () -> {
				json.writeStartObject(DumpFields.RESOURCE_ALLOCATOR);
				resources.dump(json, dconf, subflags);
				json.writeEnd();
			});
			
			dumper.add(DumpFields.ALLOCATION_REQUESTS, () -> {
				json.writeStartObject(DumpFields.ALLOCATION_REQUESTS);
				requests.dump(json, dconf, subflags);
				json.writeEnd();
			});

			dumper.run();
		});
		
		try {
			executor.submit(trigger).get();
		}
		catch (Exception exception) {
			log.warning("Dumping internal state failed!");
			log.log(Level.SEVERE, exception.getMessage(), exception);
		}
	}
	
	private static class DumpFields
	{
		private static final String NAME                = "name";
		private static final String TYPE                = "type";
		private static final String CONFIG              = "config";
		private static final String METRICS             = "metrics";
		private static final String NODE_POOL           = "node_pool";
		private static final String NODE_ALLOCATOR      = "node_allocator";
		private static final String RESOURCE_ALLOCATOR  = "resource_allocator";
		private static final String ALLOCATION_REQUESTS = "allocation_requests";
	}
	
	
	/* ==================== Internal Helpers ==================== */
	
	private void submit(Runnable task)
	{
		executor.execute(PRIORITY_DEFAULT, task);
	}
	
	private void submit(int priority, Runnable task)
	{
		executor.execute(priority, task);
	}
	
	private void schedulePoolScaling(boolean delayed, boolean oneshot)
	{
		if (oneshot) {
			if (isOneShotPoolScalingScheduled)
				return;  // Already scheduled!

			isOneShotPoolScalingScheduled = true;
		}

		// Action to execute...
		Runnable task = () -> {
			if (isShutdownRequested)
				return;
			
			isOneShotPoolScalingScheduled = false;

			this.autoscale();
			if (!oneshot)
				this.schedulePoolScaling(true, false);
		};
		
		// Action trigger...
		Runnable trigger = () -> {
			this.submit(PRIORITY_ALLOCATE, task);
		};
		
		if (delayed) {
			// Submit the action-trigger
			final long delay = poolscaler.getConfig().getPoolScalingInterval();
			scheduler.schedule(trigger, delay, TimeUnit.MILLISECONDS);
		}
		else {
			// Execute now!
			trigger.run();
		}
	}

	private void scheduleDeferredAllocationsGc(boolean delayed, boolean oneshot)
	{
		// Action to execute...
		Runnable task = () -> {
			this.cancelExpiredAllocationRequests();
			if (!oneshot)
				this.scheduleDeferredAllocationsGc(true, false);
		};
		
		// Action trigger...
		Runnable trigger = () -> {
			this.submit(PRIORITY_DEFAULT, task);
		};
		
		if (delayed) {
			// Submit the action-trigger
			final long delay = config.getDeferredAllocationsGcInterval();
			scheduler.schedule(trigger, delay, TimeUnit.MILLISECONDS);
		}
		else {
			// Execute now!
			trigger.run();
		}
	}
	
	private void scheduleRequestHistoryUpdate(boolean delayed, boolean oneshot)
	{
		// Action to execute...
		Runnable task = () -> {
			history.update();
			if (!oneshot)
				this.scheduleRequestHistoryUpdate(true, false);
		};
		
		// Action trigger...
		Runnable trigger = () -> {
			this.submit(PRIORITY_DEFAULT, task);
		};
		
		if (delayed) {
			// Submit the action-trigger
			final long delay = config.getRequestHistoryUpdateInterval();
			scheduler.schedule(trigger, delay, TimeUnit.MILLISECONDS);
		}
		else {
			// Execute now!
			trigger.run();
		}
	}
	
	private void autoscale()
	{
		final ResourceSpec missingResources = requests.getResourceSum();
		final ResourceSpec usedResources = resources.getUsedResources();
		final ResourceSpec reqResources = ResourceProvider.toPreAllocationSpec(history.getResourceSum(), config);
		
		NodePoolScaler.Action action = poolscaler.execute(pool, missingResources, reqResources, usedResources);
		if (action.getClass() == NodePoolScaler.ScaleUpAction.class) {
			Consumer<NodeID> onAllocatedCallback = (NodeID nid) -> {
				nid.setProtocol(config.getProtocol());
				try {
					this.addDnsRecord(nid);
				}
				catch (Exception error) {
					throw new RuntimeException(error);
				}
			};

			Consumer<Node> onUpCallback = (Node node) -> {
				// Task for registering the node in this provider
				final Runnable task = () -> {
					this.registerNode(node);
					this.processDeferredAllocations();
				};

				this.submit(PRIORITY_NODEMGMNT, task);
			};
			
			Consumer<ResourceSpec> onErrorCallback = (ResourceSpec spec) -> {
				// Task for updating the pending resources, on allocation failure
				Runnable task = () -> {
					pool.removePendingResources(spec);
				};
				
				this.submit(PRIORITY_NODEMGMNT, task);
			};
			
			final NodePoolScaler.ScaleUpAction sua = (NodePoolScaler.ScaleUpAction) action;
			final ResourceSpec scaleUpSpec = sua.getResourceSpec();
			final String message = "Pool needs to be scaled up by "
					+ scaleUpSpec + "!\n" + this.getNodePoolStateSummary();
			
			log.info(message);
			
			// Request more nodes!
			NodeAllocationRequest request = new NodeAllocationRequest(scaleUpSpec, onAllocatedCallback, onUpCallback, onErrorCallback);

			if (config.getDomain() != null) {
				request.setUserMetaData((String name) -> {
					DeprecatedProcessRunner processRunner = new DeprecatedProcessRunner();
					processRunner.setCommand(CLOUD_CONFIG_SCRIPT);
					processRunner.addArgument(name + "." + config.getDomain());
					processRunner.addEnvVariable("EAAS_CONFIG_PATH", CommonSingleton.configPath.toAbsolutePath().toString());
					processRunner.redirectStdErrToStdOut(false);
					processRunner.setLogger(log);
					try {
						final DeprecatedProcessRunner.Result result = processRunner.executeWithResult()
								.orElse(null);

						return (result != null && result.successful()) ? result.stdout() : null;
					}
					catch (Exception error) {
						log.log(Level.WARNING, "Generating cloud-config failed!", error);
						return null;
					}
				});
			}

			final ResourceSpec pending = nodes.allocate(request);
			pool.addPendingResources(pending);
		}
		else if (action.getClass() == NodePoolScaler.ScaleDownAction.class) {
			final NodePoolScaler.ScaleDownAction sda = (NodePoolScaler.ScaleDownAction) action;
			final String message = "Pool needs to be scaled down by "
					+ sda.getNodes().size() + " node(s)!\n" + this.getNodePoolStateSummary();
			
			log.info(message);
			
			// Shutdown nodes!
			for (NodeID nid : sda.getNodes()) {
				this.unregisterNode(nid);
				nodes.release(nid);
			}
		}
	}
	
	private String getNodePoolStateSummary()
	{
		final ResourceSpec usedResources = resources.getUsedResources();
		final ResourceSpec reqResources = history.getResourceSum();
		
		final String summary = "    Current node pool state:\n"
				+ "        PoolSize:  " + pool.getNumNodes() + " node(s), "
				                        + pool.getNumUnusedNodes() + " unused, "
				                        + pool.getNumUnhealthyNodes() + " unhealthy\n"
				+ "        Capacity:  " + pool.getCapacity() + "\n"
				+ "        Pending:   " + pool.getPendingResources() + "\n"
				+ "        Used:      " + usedResources + "\n"
				+ "        Reserved:  " + reqResources;
		
		return summary;
	}

	private void removeDnsRecord(NodeID nid)
	{
		if (config.getDomain() == null)
			return;

		final DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
		for (int i = 0; i < DNS_TRANSACTION_RETRIES_NUM; ++i) {
			runner.setCommand(DNS_UNREGISTER_SCRIPT);
			runner.addArgument(nid.getDomainName());
			runner.addArgument(nid.getIpAddress());
			runner.addEnvVariable("EAAS_CONFIG_PATH", CommonSingleton.configPath.toAbsolutePath().toString());
			runner.redirectStdErrToStdOut(true);
			runner.setLogger(log);
			if (runner.execute())
				return;  // success!

			log.warning("Removing DNS record for node '" + nid + "' failed! Retrying...");
			ResourceProvider.sleep(ResourceProvider.nextDnsRetryDelay());
		}

		log.warning("Removing DNS record for node '" + nid + "' failed!");
	}

	private void addDnsRecord(NodeID nid) throws BWFLAException
	{
		if (config.getDomain() == null)
			return;

		nid.setDomainName(nid.getSubDomainName() + "." + config.getDomain());

		final DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
		for (int i = 0; i < DNS_TRANSACTION_RETRIES_NUM; ++i) {
			runner.setCommand(DNS_REGISTER_SCRIPT);
			runner.addArgument(nid.getDomainName());
			runner.addArgument(nid.getIpAddress());
			runner.addEnvVariable("EAAS_CONFIG_PATH", CommonSingleton.configPath.toAbsolutePath().toString());
			runner.redirectStdErrToStdOut(true);
			runner.setLogger(log);
			if (runner.execute())
				return;  // success!

			log.warning("Adding DNS record for node '" + nid + "' failed! Retrying...");
			ResourceProvider.sleep(ResourceProvider.nextDnsRetryDelay());
		}

		throw new BWFLAException("DNS registration failed for node: " + nid);
	}
	
	private void registerNode(Node node)
	{
		final BiConsumer<NodeID, Boolean> onUsedStateChangedCallback = (nid, used) -> {
			log.info("Node '" + nid + "' is now " + ((used) ? "used" : "unused"));
			pool.onNodeUsedStateChanged(nid, used);
		};
		
		node.setOnUsedStateChangedCallback(onUsedStateChangedCallback);

		pool.registerNode(node);
		resources.registerNode(node);
	}
	
	private void unregisterNode(NodeID nid)
	{
		log.info("Unregistering node '" + nid + "'...");

		resources.unregisterNode(nid);
		pool.unregisterNode(nid);

		ioExecutor.execute(() -> this.removeDnsRecord(nid));
	}

	private boolean isMaxPoolSizeReached()
	{
		return poolscaler.isMaxPoolSizeReached(pool);
	}

	private void cancelExpiredAllocationRequests()
	{
		AllocationRequest allocation = null;
		int numExpiredAllocations = 0;
		
		while ((allocation = requests.peek()) != null) {
			if (!ResourceProvider.isDeadlineExpired(allocation.getDeadline()))
				break;
			
			log.info("Deadline for allocation " + allocation.getAllocationId() + " expired! Aborting...");
			++numExpiredAllocations;
			
			// Deadline expired!
			requests.poll();
			metrics.expired();
			allocation.cancel();
		}
		
		if (numExpiredAllocations > 0)
			log.info("" + numExpiredAllocations + " deferred allocation request(s) expired!");
	}

	private void cancelDeferredAllocationRequests()
	{
		AllocationRequest allocation = null;
		int numCancelledAllocations = 0;

		while ((allocation = requests.poll()) != null) {
			log.warning("Cancelling allocation " + allocation.getAllocationId() + "...");
			++numCancelledAllocations;
			allocation.cancel();
		}

		if (numCancelledAllocations > 0)
			log.info("" + numCancelledAllocations + " deferred allocation request(s) cancelled!");
	}
	
	private void processDeferredAllocations()
	{
		log.info("Processing deferred allocations...");
		
		this.cancelExpiredAllocationRequests();
		
		int numProcessed = 0;

		// Try to process all pending requests, servicable with the remaining free resources...
		final AllocationRequestQueue.View remaining = requests.filter(resources.getFreeResources());
		Iterator<AllocationRequest> iterator = remaining.iterator();
		while (iterator.hasNext()) {
			final AllocationRequest allocation = iterator.next();
			final UUID aid = allocation.getAllocationId();
			try {
				ResourceHandle handle = resources.allocate(aid, allocation.getResourceSpec());
				if (handle == null)
					continue;  // Allocation failed, try the next one!
				
				log.info("Resources for " + aid + " allocated on node '" + handle.getNodeID() + "'");
				allocation.complete(handle);
				++numProcessed;
			}
			catch (Throwable throwable) {
				log.log(Level.WARNING, "Deferred allocation " + aid + " failed!\n", throwable);
				allocation.abort(throwable);
				metrics.failed();
			}
			
			iterator.remove();
			
			// Update the bound and try the remaining requests
			remaining.update(resources.getFreeResources());
			iterator = remaining.iterator(); 
		}
		
		log.info("" + numProcessed + " deferred allocation(s) processed, " + requests.size() + " left");

		// If the node pool has reached its max. size, then all deferred
		// allocation requests probably can't be processed anymore!
		if (this.isMaxPoolSizeReached() && !requests.isEmpty()) {
			log.warning("Max. pool size reached! Cancelling all deferred allocation request(s)...");
			this.cancelDeferredAllocationRequests();
		}
	}
	
	public static long getCurrentTime()
	{
		return System.currentTimeMillis();
	}
	
	private static boolean isDeadlineExpired(long deadline)
	{
		return (ResourceProvider.getCurrentTime() >= deadline);
	}
	
	private static ResourceSpec toPreAllocationSpec(ResourceSpec requested, ResourceProviderConfig config)
	{
		final MutableResourceSpec spec = new MutableResourceSpec(requested);
		spec.scale(config.getPreAllocationRequestHistoryMultiplier());
		spec.max(config.getPreAllocationMinBound());
		spec.min(config.getPreAllocationMaxBound());
		return spec;
	}

	private static long nextDnsRetryDelay()
	{
		final Random random = new Random();
		return (long) (500 + random.nextInt(1500));
	}

	private static void sleep(long timeout)
	{
		try {
			Thread.sleep(timeout);
		}
		catch (Exception error) {
			// Ignore it!
		}
	}

	
	private class ShutdownTask implements Runnable
	{
		private final CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();
		private final List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		private boolean success = true;

		public CompletableFuture<Boolean> completion()
		{
			return result;
		}

		@Override
		public void run()
		{
			// Release all registered nodes...
			final Collection<Node> poolNodes = new ArrayList<Node>(pool.getAllNodes());
			for (Node node : poolNodes) {
				final NodeID nid = node.getId();
				ResourceProvider.this.unregisterNode(nid);
				results.add(nodes.release(nid));
			}

			// Check pending results...
			final Predicate<Future<Boolean>> filter = (future) -> {
				if (!future.isDone())
					return false;

				try {
					success &= future.get();
				}
				catch (Exception exception) {
					log.log(Level.WARNING, "Releasing node failed!", exception);
				}

				return true;
			};

			results.removeIf(filter);

			// Wait until finished...
			if (!results.isEmpty() || pool.hasPendingResources()) {
				// Retry later...
				Runnable trigger = () -> ResourceProvider.this.submit(this);
				scheduler.schedule(trigger, 5, TimeUnit.SECONDS);
			}
			else {
				result.complete(success);
			}
		}
	}
}
