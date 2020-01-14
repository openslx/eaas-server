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

package de.bwl.bwfla.eaas.cluster;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.NotFoundException;

import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;
import de.bwl.bwfla.eaas.cluster.config.ClusterManagerConfig;
import de.bwl.bwfla.eaas.cluster.config.ResourceProviderConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;
import de.bwl.bwfla.eaas.cluster.exception.AllocationFailureException;
import de.bwl.bwfla.eaas.cluster.exception.OutOfResourcesException;
import de.bwl.bwfla.eaas.cluster.exception.QuotaExceededException;
import de.bwl.bwfla.eaas.cluster.metadata.Label;
import de.bwl.bwfla.eaas.cluster.metadata.LabelSelector;
import de.bwl.bwfla.eaas.cluster.metadata.Labels;
import de.bwl.bwfla.eaas.cluster.provider.IResourceProvider;
import de.bwl.bwfla.eaas.cluster.provider.ResourceProvider;
import de.bwl.bwfla.eaas.cluster.provider.ResourceProviderComparators;
import de.bwl.bwfla.eaas.cluster.tenant.TenantManager;


@ApplicationScoped
public class ClusterManager implements IClusterManager
{
	private PrefixLogger log;
	private ClusterManagerConfig config;
	private ClusterManagerExecutors executors;
	private Map<String, IResourceProvider> providers;
	private Comparator<IResourceProvider> comparator;

	@Inject
	private TenantManager tenants = null;


	public ClusterManager(ClusterManagerConfig config, ClusterManagerExecutors executors, Comparator<IResourceProvider> comparator)
	{
		this.initialize(config, executors, comparator);
	}


	/* ========== IClusterManager Implementation ========== */

	@Override
	public ResourceHandle allocate(String tenant, UUID aid, ResourceSpec spec, Duration duration)
			throws TimeoutException, AllocationFailureException
	{
		return this.allocate(tenant, aid, spec, duration.toMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public ResourceHandle allocate(String tenant, UUID aid, ResourceSpec spec, long timeout, TimeUnit unit)
			throws TimeoutException, AllocationFailureException, OutOfResourcesException
	{
		return this.allocate(tenant, Collections.emptyList(), aid, spec, timeout, unit);
	}
	
	@Override
	public ResourceHandle allocate(String tenant, LabelSelector selector, UUID aid, ResourceSpec spec, Duration duration)
			throws TimeoutException, AllocationFailureException
	{
		return this.allocate(tenant, selector, aid, spec, duration.toMillis(), TimeUnit.MILLISECONDS);
	}
	
	@Override
	public ResourceHandle allocate(
			String tenant, LabelSelector selector, UUID aid, ResourceSpec spec, long timeout, TimeUnit unit)
			throws TimeoutException, AllocationFailureException
	{
		Collection<LabelSelector> selectors = new ArrayList<LabelSelector>(1);
		selectors.add(selector);
		
		return this.allocate(tenant, selectors, aid, spec, timeout, unit);
	}
	
	@Override
	public ResourceHandle allocate(
			String tenant, Collection<LabelSelector> selectors, UUID aid, ResourceSpec spec, Duration duration)
			throws TimeoutException, AllocationFailureException
	{
		return this.allocate(tenant, selectors, aid, spec, duration.toMillis(), TimeUnit.MILLISECONDS);
	}
	
	@Override
	public ResourceHandle allocate(String tenant, Collection<LabelSelector> selectors,
			UUID aid, ResourceSpec spec, long timeout, TimeUnit unit)
			throws TimeoutException, AllocationFailureException
	{
		// Check tenant's quota...
		if (tenant != null && !tenants.allocate(tenant, spec))
			throw new QuotaExceededException("Quota exceeded for tenant " + tenant);

		try {
			return this.doAllocation(selectors, aid, spec, timeout, unit)
					.setTenantId(tenant);
		}
		catch (TimeoutException | AllocationFailureException error) {
			// Revert quota allocation!
			if (tenant != null)
				tenants.free(tenant, spec);

			throw error;
		}
	}

	@Override
	public void release(ResourceHandle handle)
	{
		final String name = handle.getProviderName();
		final IResourceProvider provider = providers.get(name);
		if (provider == null)
			throw new IllegalArgumentException("Unknown resource handle: " + handle.toString());

		// Update tenant's quota allocation
		final Consumer<ResourceSpec> finalizer = (spec) -> {
			try {
				final String tenant = handle.getTenantId();
				if (tenant != null)
					tenants.free(tenant, spec);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Updating tenant's quota usage failed!", error);
			}
		};

		provider.release(handle)
				.thenAccept(finalizer);
	}
	
	@Override
	public CompletableFuture<Boolean> shutdown()
	{
		if (providers.isEmpty()) {
			log.info("No resource providers to shut down");
			return CompletableFuture.completedFuture(true);
		}
		
		final int numProviders = providers.size();
		final ShutdownResultHandler handler = new ShutdownResultHandler(numProviders, log);
		
		log.info("Shutting down " + numProviders + " resource provider(s)...");
		
		for (IResourceProvider provider : providers.values()) {
			log.info("Shutting down '" + provider.getName() + "' resource provider...");
			provider.shutdown()
					.whenComplete((result, error) -> handler.accept(provider, result, error));
		}
		
		return handler.completion();
	}
	
	@Override
	public void setResourceProviderComparator(Comparator<IResourceProvider> comparator)
	{
		this.comparator = comparator;
	}
	
	@Override
	public Comparator<IResourceProvider> getResourceProviderComparator()
	{
		return comparator;
	}
	
	public String getName()
	{
		return config.getName();
	}
	
	public Collection<String> getProviderNames()
	{
		return Collections.unmodifiableCollection(providers.keySet());
	}
	
	public boolean checkAccessToken(String token)
	{
		final String exptoken = config.getAdminApiAccessToken();
		return exptoken.contentEquals(token);
	}
	
	
	/* ========== Admin REST-API ========== */
	
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

				case "providers":
					if (!dconf.hasMoreUrlSegments()) {
						// Dump all providers as array...
						json.writeStartArray();
						for (IResourceProvider provider : providers.values())
							provider.dump(json, dconf, flags);

						json.writeEnd();
					}
					else {
						// Dump a specific provider only...
						final String name = dconf.nextUrlSegment();
						final IResourceProvider provider = providers.get(name);
						if (provider == null)
							throw new NotFoundException("Provider '" + name + "' was not found!");

						provider.dump(json, dconf, flags);
					}

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

			dumper.add(DumpFields.PROVIDERS, () -> {
				final int subflags = DumpFlags.reset(flags, DumpFlags.INLINED);
				json.write("num_" + DumpFields.PROVIDERS, providers.size());
				json.writeStartArray(DumpFields.PROVIDERS);
				for (IResourceProvider provider : providers.values())
					provider.dump(json, dconf, subflags);

				json.writeEnd();
			});

			dumper.run();
		});
		
		trigger.run();
	}

	private static class DumpFields
	{
		private static final String CONFIG     = "config";
		private static final String PROVIDERS  = "providers";
	}


	/* ========== Internal Helpers ========== */

	/** Constructor for CDI */
	protected ClusterManager()
	{
		// Empty!
	}
	
	private void initialize(ClusterManagerConfig config, ClusterManagerExecutors executors, Comparator<IResourceProvider> comparator)
	{
		final PrefixLoggerContext logContext = new PrefixLoggerContext()
				.add("CM", config.getName());
		
		this.log = new PrefixLogger(this.getClass().getName(), logContext);
		this.config = config;
		this.executors = executors;
		this.providers = new HashMap<String, IResourceProvider>();
		this.comparator = comparator;

		final List<ResourceProviderConfig> providerConfigs = config.getResourceProviderConfigs();
		log.info("Initializing " + providerConfigs.size() + " resource provider(s)...");
		
		// Initialize the resource providers...
		for (ResourceProviderConfig rpc : providerConfigs) {
			try {
				final String name = rpc.getName();
				log.info("Initializing '" + name + "' resource provider...");
				final ResourceProvider provider = new ResourceProvider(rpc, executors);
				final String rank = provider.getLabelIndex().get(Labels.RANK);
				providers.put(name, provider);
				log.info("Rank " + rank + " assigned to resource provider '" + name + "'");
				log.info("Resource provider '" + name + "' (" + rpc.getType() + ") initialized");
			}
			catch (Exception exception) {
				final String message = "Initializing resource provider '"
						+ rpc.getName() + "' failed!\n";
				
				log.log(Level.WARNING, message, exception);
			}
		}
	}
	
	private ResourceHandle doAllocation(IResourceProvider provider, UUID aid,
			ResourceSpec spec, boolean scaleup, long timeout, TimeUnit unit)
			throws TimeoutException, OutOfResourcesException, AllocationFailureException
	{
		final String aprefix = (scaleup) ? "scaleup-" : "";
		log.info("Starting " + aprefix + "allocation " + aid + " using provider '" + provider.getName() + "'...");
		
		CompletableFuture<ResourceHandle> result = provider.allocate(aid, spec, scaleup, timeout, unit);
		try {
			final ResourceHandle handle = result.get(timeout, unit);
			final String message = "Allocation " + aid + " assigned to node " + handle.getNodeID()
					+ " from provider '" + provider.getName() + "'";
			
			log.info(message);
			return handle;
		}
		catch (TimeoutException exception) {
			// Release the allocated resources, when allocation is done!
			result.thenAccept((handle) -> provider.release(handle));
			log.warning("Allocation " + aid + " timed out! Aborting...");
			throw exception;
		}
		catch (Throwable exception) {
			if (exception.getCause() instanceof OutOfResourcesException)
				throw (OutOfResourcesException) exception.getCause();
			else throw new AllocationFailureException(exception);
		}
	}
	
	private ResourceHandle doAllocation(Collection<IResourceProvider> candidates, 
			UUID aid, ResourceSpec spec, boolean scaleup, long deadline)
			throws TimeoutException, OutOfResourcesException
	{
		for (IResourceProvider provider : candidates) {
			try {
				final long timeout = deadline - ResourceProvider.getCurrentTime();
				return this.doAllocation(provider, aid, spec, scaleup, timeout, TimeUnit.MILLISECONDS);
			}
			catch (TimeoutException error) {
				// No time left to continue!
				throw error;
			}
			catch (Throwable error) {
				// Ignore it!
			}
		}
		
		throw new OutOfResourcesException();
	}

	private ResourceHandle doAllocation(Collection<LabelSelector> selectors,
			UUID aid, ResourceSpec spec, long timeout, TimeUnit unit)
			throws TimeoutException, AllocationFailureException
	{
		final Set<IResourceProvider> candidates = new TreeSet<IResourceProvider>(comparator);
		if (selectors == null || selectors.isEmpty()) {
			// No provider filtering should be done!
			candidates.addAll(providers.values());
		}
		else {
			// Perform provider filtering...
			providers.values().stream()
					.filter((provider) -> provider.apply(selectors))
					.forEach((provider) -> candidates.add(provider));
		}

		final int numSelectedProviders = candidates.size();
		final int numProviders = providers.size();
		log.info(numSelectedProviders + " out of " + numProviders + " provider(s) selected for allocation " + aid);
		if (numSelectedProviders == 0) {
			final String message = "Allocation " + aid + " failed! No providers found matching selector(s)";
			final StringBuilder sb = new StringBuilder()
					.append(message)
					.append(":\n");

			for (LabelSelector selector : selectors) {
				sb.append("    ")
						.append(selector.toString())
						.append("\n");
			}

			log.warning(sb.toString());
			throw new AllocationFailureException(message + ".");
		}

		final long deadline = ResourceProvider.getCurrentTime() + unit.toMillis(timeout);
		if (numSelectedProviders > 1) {
			try {
				// Multiple providers are available!
				// Don't scale up the resources, before trying each provider...
				return this.doAllocation(candidates, aid, spec, false, deadline);
			}
			catch (TimeoutException error) {
				// No time left to continue!
				throw error;
			}
			catch (Throwable error) {
				// Ignore it!
			}
		}

		// Try to allocate and scale up the resources, when possible...
		return this.doAllocation(candidates, aid, spec, true, deadline);
	}

	@PostConstruct
	protected void initialize()
	{
		ClusterManagerConfig config = new ClusterManagerConfig();
		config.load();
		config.validate();
		
		Comparator<IResourceProvider> comparator = ResourceProviderComparators.RANK_COMPARATOR;
		
		// Add rank labels to provider configs, if not already present...
		{
			final Collection<ResourceProviderConfig> rpconfigs = config.getResourceProviderConfigs();
			int rank = rpconfigs.size();
			
			for (ResourceProviderConfig rpc : config.getResourceProviderConfigs()) {
				final Collection<Label> labels = rpc.getLabels();
				final boolean found = labels.stream()
						.anyMatch((label) -> Labels.RANK.equals(label.getKey()));
				
				if (found)
					continue;
				
				final String rankstr = Integer.toString(rank);
				labels.add(new Label(Labels.RANK, rankstr));
				++rank;
			}
		}
		
		this.initialize(config, new ClusterManagerExecutors(), comparator);
	}
	
	@PreDestroy
	protected void terminate()
	{
		if (providers.isEmpty()) {
			log.info("No resource providers to terminate");
			return;
		}
		
		final int numPoolThreads = Math.min(8, Runtime.getRuntime().availableProcessors());
		final ExecutorService executor = Executors.newFixedThreadPool(numPoolThreads);
		final AtomicInteger numFailedProviders = new AtomicInteger(0);
		final int numProviders = providers.size();
		
		log.info("Terminating " + numProviders + " resource provider(s)...");
		
		for (IResourceProvider provider : providers.values()) {
			final Runnable task = () -> {
				log.info("Terminating '" + provider.getName() + "' resource provider...");
				if (provider.terminate())
					return;

				numFailedProviders.incrementAndGet();

				final String message = "Termination of resource provider '"
						+ provider.getName() + "' failed!";

				log.warning(message);
			};
			
			executor.execute(task);
		}
		
		// Wait for the executor to terminate...
		try {
			executor.shutdown();
			if (!executor.awaitTermination(20, TimeUnit.SECONDS))
				log.warning("Termination timed out!");
			else {
				final int failed = numFailedProviders.get();
				if (failed == 0) {
					log.info("All resource providers terminated");
					return;
				}

				final String message = new StringBuilder(512)
						.append("Termination of ")
						.append(failed)
						.append(" out of ")
						.append(numProviders)
						.append(" resource provider(s) failed!")
						.toString();

				log.warning(message);
			}
		}
		catch (InterruptedException exception) {
			log.warning("Termination was interrupted!");
		}
		
		log.warning("NOT ALL RESOURCES MAY BE CLEANED UP PROPERLY!");
	}

	private static class ShutdownResultHandler
	{
		private final CompletableFuture<Boolean> completion;
		private final int numExpectedResults;
		private final AtomicInteger numMissingResults;
		private final AtomicInteger numFailedResults;
		private final Logger log;
		
		public ShutdownResultHandler(int numExpectedResults, Logger log)
		{
			this.completion = new CompletableFuture<Boolean>();
			this.numExpectedResults = numExpectedResults;
			this.numMissingResults = new AtomicInteger(numExpectedResults);
			this.numFailedResults = new AtomicInteger(0);
			this.log = log;
		}
		
		public CompletableFuture<Boolean> completion()
		{
			return completion;
		}
		
		public void accept(IResourceProvider provider, boolean result, Throwable error)
		{
			if (error != null) {
				final String message = "Shutdown of resource provider '"
						+ provider.getName() + "' failed!\n";
				
				log.log(Level.WARNING, message, error);
				result = false;
			}
			
			if (!result)
				numFailedResults.incrementAndGet();
			
			// All results collected?
			if (numMissingResults.decrementAndGet() != 0)
				return;  // Not yet
			
			// Last caller prints the final result
			final int failed = numFailedResults.get();
			if (failed > 0) {
				final String message = new StringBuilder(512)
						.append("Shutdown of ")
						.append(failed)
						.append(" out of ")
						.append(numExpectedResults)
						.append(" resource provider(s) failed!")
						.toString();
				
				log.warning(message);
			}
			else {
				log.info("All resource providers shut down properly");
			}
		}
	}
}
