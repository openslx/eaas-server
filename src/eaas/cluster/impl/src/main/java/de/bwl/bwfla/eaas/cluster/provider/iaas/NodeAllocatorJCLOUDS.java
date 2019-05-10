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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.eaas.cluster.provider.NodeAllocationRequest;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.compute.predicates.NodePredicates;
import org.jclouds.logging.jdk.config.JDKLoggingModule;
import org.jclouds.openstack.keystone.auth.config.CredentialTypes;
import org.jclouds.openstack.keystone.config.KeystoneProperties;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

import de.bwl.bwfla.common.concurrent.SequentialExecutor;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;
import de.bwl.bwfla.eaas.cluster.ClusterManagerExecutors;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.ResourceSpec.MemoryUnit;
import de.bwl.bwfla.eaas.cluster.config.NodeAllocatorConfigJCLOUDS;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;
import de.bwl.bwfla.eaas.cluster.provider.Node;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;


public class NodeAllocatorJCLOUDS implements INodeAllocator
{
	// Template variables
	public static final String TVAR_ADDRESS = "{{address}}";

	// NodeInfo metadata variables
	public static final String MDVAR_VMNAME = "vm_name";
	public static final String MDVAR_VMID   = "vm_id";

	protected final Logger log;

	protected final NodeAllocatorConfigJCLOUDS config;
	protected final ComputeService compute;
	protected final Template nodeTemplate;
	protected final ResourceSpec nodeCapacity;
	protected final Map<NodeID, NodeInfo> nodeRegistry;
	protected final NodeNameGenerator nodeNameGenerator;
	protected final NodeHealthChecker nodeHealthChecker;
	protected final Consumer<NodeID> onDownCallback;
	protected final ClusterManagerExecutors executors;
	protected final SequentialExecutor tasks;
	private final AtomicInteger numAllocationRequests;

	public NodeAllocatorJCLOUDS(NodeAllocatorConfigJCLOUDS config, Consumer<NodeID> onDownCallback,
								ClusterManagerExecutors executors, PrefixLoggerContext parentLogContext) throws MalformedURLException
	{
		final PrefixLoggerContext logContext = new PrefixLoggerContext(parentLogContext)
				.add("NA", config.getProviderType());

		this.log = new PrefixLogger(this.getClass().getName(), logContext);
		this.config = config;
		this.compute = NodeAllocatorJCLOUDS.newComputeService(config);
		this.nodeTemplate = NodeAllocatorJCLOUDS.newNodeTemplate(compute, config);
		this.nodeCapacity = NodeAllocatorJCLOUDS.toNodeCapacity(nodeTemplate);
		this.nodeRegistry = new HashMap<NodeID, NodeInfo>();
		this.nodeNameGenerator = new NodeNameGenerator(config.getNodeNamePrefix());
		this.onDownCallback = onDownCallback;
		this.executors = executors;
		this.tasks = new SequentialExecutor(log, executors.computation(), 64);
		this.numAllocationRequests = new AtomicInteger(0);

		log.info("Using machine type '" + nodeTemplate.getHardware().getName() + "' with spec: " + nodeCapacity);

		this.nodeHealthChecker = new NodeHealthChecker(
				Collections.unmodifiableCollection(nodeRegistry.values()),
				config,
				(nid) -> { onDownCallback.accept(nid); this.release(nid); },
				(delayed) -> this.scheduleHealthChecking(delayed),
				executors.io(),
				log);

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

		if (request.getOnErrorCallback() == null || request.getOnErrorCallback() == null)
			throw new IllegalArgumentException("No callbacks specified!");

		final long startTimestamp = System.currentTimeMillis();

		// Compute the number of nodes to start...
		final int numRequestedNodes = NodeAllocatorUtil.computeNumRequiredNodes(request.getSpec(), nodeCapacity);
		final ResourceSpec pending = ResourceSpec.create(numRequestedNodes, nodeCapacity);

		final Runnable task = () -> {
			final AllocationResultHandler result =
					new AllocationResultHandler(numRequestedNodes, pending, request.getOnErrorCallback(), log);

			final Set<? extends NodeMetadata> nodes = this.makeVmCreateRequest(request, numRequestedNodes);
			for (int i = numRequestedNodes - nodes.size(); i > 0; --i)
				result.onNodeFailure();

			// Functor for waiting until the node is reachable
			final Function<NodeInfo, CompletableFuture<NodeInfo>> checkReachabilityFtor = (info) -> {
				log.info("Waiting for node '" + info.getNodeId() + "' to become reachable...");
				final NodeReachabilityPollTask check = new NodeReachabilityPollTask.Builder()
						.setPollInterval(config.getVmBootPollInterval(), TimeUnit.MILLISECONDS)
						.setPollIntervalDelta(config.getVmBootPollIntervalDelta(), TimeUnit.MILLISECONDS)
						.setMaxNumRetries(config.getVmMaxNumBootPolls())
						.setNodeAllocatorConfig(config)
						.setScheduler(executors.scheduler())
						.setExecutor(executors.io())
						.setLogger(log)
						.setNodeInfo(info)
						.build();

				executors.io().execute(check);
				return check.completion();
			};

			// Functor for handling success outcomes of boot requests
			final Consumer<NodeInfo> checkResultAction = (info) -> {
				final Node node = info.getNode();
				final NodeID nid = node.getId();
				if (node.isHealthy()) {
					log.info("Node '" + nid + "' is up and reachable");
					this.submit(() -> nodeRegistry.put(nid, info));
					result.onNodeReady(node.getCapacity());
					request.getOnUpCallback().accept(node);
				}
				else {
					// Node seems to be unreachable, remove the corresponding VM!
					log.info("Node '" + nid + "' is unreachable after boot!");
					final CompletionTrigger<NodeInfo> trigger = new CompletionTrigger<NodeInfo>(info);
					this.makeVmDeleteRequest(trigger.completion());
					trigger.submit(executors.io());
					result.onNodeFailure();
				}

				long duration = System.currentTimeMillis() - startTimestamp;
				duration = TimeUnit.MILLISECONDS.toSeconds(duration);
				log.info("Allocating node '" + nid + "' took " + duration + " second(s)");
			};

			// Submit all requests...
			for (NodeMetadata node : nodes) {
				final String name = "vm-allocation-" + numAllocationRequests.incrementAndGet();
				final CleanupHandlerChain cleanups = new CleanupHandlerChain(name, log);

				// Register a cleanup handler for VM instance
				{
					final Runnable handler = () -> {
						final String vmname = node.getName();
						log.info("Deleting VM '" + vmname + "'...");
						compute.destroyNode(node.getId());
						log.info("VM '" + vmname + "' deleted");
					};

					cleanups.add(handler);
				}

				// Functor for handling error outcome of a boot request
				final BiConsumer<Void, Throwable> checkErrorAction = (unused, error) -> {
					if (error == null)
						return;

					log.log(Level.WARNING, "Starting new VM failed!\n", error);
					result.onNodeFailure();
					cleanups.execute();
				};

				log.info("VM '" + node.getName() + "' created");

				final CompletionTrigger<NodeMetadata> trigger = new CompletionTrigger<NodeMetadata>(node);
				this.makeNodeInfo(trigger.completion(), cleanups)
						.thenCompose(checkReachabilityFtor)
						.thenAccept(checkResultAction)
						.whenComplete(checkErrorAction);

				trigger.submit(executors.io());
			}
		};

		executors.io().execute(task);
		return pending;
	}

	@Override
	public CompletableFuture<Boolean> release(NodeID nid)
	{
		if (nid == null)
			throw new IllegalArgumentException("Invalid node ID specified!");

		final CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();

		final Runnable task = () -> {
			final NodeInfo info = nodeRegistry.remove(nid);
			if (info == null) {
				result.complete(false);
				return;
			}

			final Map<String, Object> metadata = info.getMetadata();
			final String vmname = (String) metadata.get(MDVAR_VMNAME);
			log.info("Releasing node '" + info.getNodeId() + "' (" + vmname + ")...");

			final CompletionTrigger<NodeInfo> trigger = new CompletionTrigger<NodeInfo>(info);
			this.makeVmDeleteRequest(trigger.completion())
					.whenComplete((value, error) -> result.complete(value));

			trigger.submit(executors.io());
		};

		this.submit(task);
		return result;
	}

	@Override
	public boolean terminate()
	{
		// Cleanup according to JClouds internal state...
		final Predicate<NodeMetadata> filter = NodePredicates.inGroup(config.getNodeGroupName());
		final Collection<NodeInfo> knownNodes = nodeRegistry.values();
		final Set<String> destroyedNodes = new LinkedHashSet<String>();
		{
			knownNodes.forEach((node) -> {
				final Map<String, Object> metadata = node.getMetadata();
				final String id = (String) metadata.get(MDVAR_VMID);
				log.info("Deleting node " + metadata.get(MDVAR_VMNAME) + "...");
				destroyedNodes.add(id);
				compute.destroyNode(id);
			});
		}

		final int numDestroyedNodes = destroyedNodes.size();
		final int numKnownNodes = knownNodes.size();

		if (numKnownNodes == 0) {
			log.info("No VMs found to be deleted");
			return true;
		}

		final StringBuilder sb = new StringBuilder(2048);
		if (numDestroyedNodes < numKnownNodes) {
			final int numFailures = numKnownNodes - numDestroyedNodes;
			sb.append("Destroying ")
					.append(numFailures)
					.append(" out of ")
					.append(numKnownNodes)
					.append(" VM(s) failed!")
					.append("\n\n")
					.append("    Failed VM IDs:\n");

			final Set<String> failedNodes = new LinkedHashSet<String>();
			knownNodes.forEach((node) -> {
				final String vmid = (String) node.getMetadata().get(MDVAR_VMID);
				if (!destroyedNodes.contains(vmid))
					failedNodes.add(vmid);
			});

			final String spacer = "        ";
			for (String node : failedNodes)
				sb.append(spacer).append(node);

			sb.append("\n");
			log.warning(sb.toString());
		}
		else {
			log.info(numDestroyedNodes + " out of " + numKnownNodes + " VM(s) destroyed");
		}

		// VM summary message...
		{
			final String spacer = "\n    ";
			sb.setLength(0);
			sb.append("Complete list of VMs managed by this node allocator:");
			for (NodeInfo node : knownNodes) {
				final String vmid = (String) node.getMetadata().get(MDVAR_VMID);
				sb.append(spacer).append(vmid);
			}

			sb.append('\n');
			log.info(sb.toString());
		}

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

				case "nodes":
					json.writeStartArray();
					for (NodeInfo node : nodeRegistry.values())
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

			dumper.add(DumpFields.NODES, () -> {
				json.write("num_" + DumpFields.NODES, nodeRegistry.size());
				json.writeStartArray(DumpFields.NODES);
				for (NodeInfo node : nodeRegistry.values())
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
		private static final String NODES          = "nodes";
	}


	/* ==================== Internal Helpers ==================== */

	private void submit(Runnable task)
	{
		tasks.execute(task);
	}

	private static ComputeService newComputeService(NodeAllocatorConfigJCLOUDS config)
	{
		final Iterable<Module> modules = ImmutableSet.<Module>of(new JDKLoggingModule());
		final Properties overrides = new Properties();
		String identity, credential, endpoint;
		switch (config.getProviderType()) {
			case NodeAllocatorConfigJCLOUDS.ProviderConfigOPENSTACK.TYPE:
				final NodeAllocatorConfigJCLOUDS.ProviderConfigOPENSTACK osconfig = config.getProviderConfig()
						.as(NodeAllocatorConfigJCLOUDS.ProviderConfigOPENSTACK.class);

				overrides.setProperty(KeystoneProperties.CREDENTIAL_TYPE, CredentialTypes.PASSWORD_CREDENTIALS);
				overrides.setProperty(KeystoneProperties.KEYSTONE_VERSION, osconfig.getAuthApiVersion());
				overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");
				if (osconfig.getAuthApiVersion().equals("3"))
					overrides.setProperty(KeystoneProperties.SCOPE, "project:" + osconfig.getAuthProjectName());

				identity = osconfig.getAuthUser();
				credential = osconfig.getAuthPassword();
				endpoint = osconfig.getAuthEndpoint();

				break;

			default:
				throw new IllegalStateException("Unknown provider-type: " + config.getProviderType());
		}

		final ComputeServiceContext context = ContextBuilder.newBuilder(config.getProviderType())
				.credentials(identity, credential)
				.endpoint(endpoint)
				.overrides(overrides)
				.modules(modules)
				.buildView(ComputeServiceContext.class);

		return context.getComputeService();
	}

	private static Template newNodeTemplate(ComputeService compute, NodeAllocatorConfigJCLOUDS config)
	{
		return compute.templateBuilder()
				.hardwareId(config.getVmHardwareId())
				.imageId(config.getVmImageId())
				.build();
	}

	private static ResourceSpec toNodeCapacity(Template nodeTemplate)
	{
		final Hardware hardware = nodeTemplate.getHardware();
		final float cpu = (float) hardware.getProcessors()
				.stream()
				.mapToDouble((processor) -> processor.getCores())
				.sum();

		return ResourceSpec.create(cpu, hardware.getRam(), MemoryUnit.MEGABYTES);
	}

	private Set<? extends NodeMetadata> makeVmCreateRequest(NodeAllocationRequest request, int numRequestedNodes)
	{
		try {
			log.info("Starting " + numRequestedNodes + " requested node(s)...");

			// Prepare a list of names for requested nodes...
			final List<String> names = new ArrayList<String>(numRequestedNodes);
			for (int i = 0; i < numRequestedNodes; ++i)
				names.add(nodeNameGenerator.next());

			// Update options for node template...
			final TemplateOptions options = nodeTemplate.getOptions()
					.securityGroups(config.getSecurityGroupName())
					.networks(config.getVmNetworkId())
					.nodeNames(names);

			switch (config.getProviderType()) {
				case NodeAllocatorConfigJCLOUDS.ProviderConfigOPENSTACK.TYPE:
					if (request.getUserMetaData() != null) {
						final String userdata = request.getUserMetaData().apply("*");
						options.as(NovaTemplateOptions.class)
								.userData(userdata.getBytes());
					}
					break;
			}

			// Update template...
			final Template template = compute.templateBuilder()
					.fromTemplate(nodeTemplate)
					.options(options)
					.build();

			return compute.createNodesInGroup(config.getNodeGroupName(), numRequestedNodes, template);
		}
		catch (RunNodesException exception) {
			final BiConsumer<NodeMetadata, Throwable> destroyer = (node, error) -> {
				log.log(Level.WARNING, "Creating VM '" + node.getName() + "' failed!\n", error);
				compute.destroyNode(node.getId());
			};

			exception.getNodeErrors().forEach(destroyer);
			return exception.getSuccessfulNodes();
		}
	}

	private CompletableFuture<Boolean> makeVmDeleteRequest(CompletableFuture<NodeInfo> trigger)
	{
		final Function<NodeInfo, Boolean> functor = (info) -> {
			final Map<String, Object> metadata = info.getMetadata();
			final String vmid = (String) metadata.get(MDVAR_VMID);
			final String vmname = (String) metadata.get(MDVAR_VMNAME);
			try {
				log.info("Deleting VM '" + vmname + "'...");
				compute.destroyNode(vmid);
				log.info("VM '" + vmname + "' deleted");
				return true;
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Deleting VM '" + vmname + "' failed!\n", error);
				return false;
			}
		};

		return trigger.thenApply(functor);
	}

	private CompletableFuture<NodeInfo> makeNodeInfo(CompletableFuture<NodeMetadata> trigger, CleanupHandlerChain cleanups)
	{
		final Function<NodeMetadata, NodeInfo> functor = (instance) -> {
			final String vmname = instance.getName();

			// Lookup the public IP address of the instance
			final String extip = instance.getPublicAddresses()
					.iterator()
					.next();

			if (extip == null || extip.isEmpty())
				throw new IllegalStateException("No external IP address for VM '" + vmname + "' found!");

			log.info("VM '" + vmname + "' is running. External IP is " + extip);

			// Compute the URL for health checks and create node
			final String urlTemplate = config.getHealthCheckUrl();
			final String healthCheckUrl = urlTemplate.replace(TVAR_ADDRESS, extip);
			final Node node = new Node(new NodeID(extip), nodeCapacity);
			try {
				final NodeInfo info = new NodeInfo(node, healthCheckUrl);
				final Map<String, Object> metadata = info.getMetadata();
				metadata.put(MDVAR_VMID, instance.getId());
				metadata.put(MDVAR_VMNAME, vmname);
				return info;
			}
			catch (Exception error) {
				throw new CompletionException(error);
			}
		};

		return trigger.thenApply(functor);
	}

	private void scheduleHealthChecking(boolean delayed)
	{
		// Action trigger...
		final Runnable trigger = () -> {
			this.submit(nodeHealthChecker);
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
