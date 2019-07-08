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

package de.bwl.bwfla.eaas.cluster.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.json.stream.JsonGenerator;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.WithPropertyConverter;

import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.config.util.DurationPropertyConverter;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;
import de.bwl.bwfla.eaas.cluster.metadata.Label;


public class ResourceProviderConfig extends BaseConfig
{
	/** Type for the blades-provider */
	public static final String TYPE_BLADES   = "blades";
	public static final String TYPE_GCE      = "gce";
	public static final String TYPE_JCLOUDS  = "jclouds";
	
	/** All supported provider types */
	private static final Map<String, Supplier<NodeAllocatorConfig>> NODE_ALLOCATOR_TYPES;
	static {
		NODE_ALLOCATOR_TYPES = new TreeMap<String, Supplier<NodeAllocatorConfig>>();
		NODE_ALLOCATOR_TYPES.put(TYPE_BLADES , () -> new NodeAllocatorConfigBLADES());
		NODE_ALLOCATOR_TYPES.put(TYPE_GCE    , () -> new NodeAllocatorConfigGCE());
		NODE_ALLOCATOR_TYPES.put(TYPE_JCLOUDS, () -> new NodeAllocatorConfigJCLOUDS());
	}
	
	/** Supported pool scaler per provider type */
	private static final Map<String, Supplier<NodePoolScalerConfig>> POOLSCALER_TYPES;
	static {
		final Supplier<NodePoolScalerConfig> homoNodePoolScalerConfig =
				() -> new HomogeneousNodePoolScalerConfig();
		
		POOLSCALER_TYPES = new TreeMap<String, Supplier<NodePoolScalerConfig>>();
		POOLSCALER_TYPES.put(TYPE_BLADES , homoNodePoolScalerConfig);
		POOLSCALER_TYPES.put(TYPE_GCE    , homoNodePoolScalerConfig);
		POOLSCALER_TYPES.put(TYPE_JCLOUDS, homoNodePoolScalerConfig);
	}


	// Config parameters
	
	@Config("name")
	private String name = null;

	@Config("type")
	private String type = null;

	@Config("domain")
	private String domain = null;

	@Config("protocol")
	private String protocol = null;
	
	@Config("deferred_allocations_gc_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long deferredAllocationsGcInterval = -1L;

	@Config("request_history.update_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long reqHistoryUpdateInterval = -1L;
	
	@Config("request_history.max_request_age")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long reqHistoryMaxRequestAge = -1L;

	@Config("request_history.max_number_requests")
	private int reqHistoryMaxNumRequests = -1;

	@Config("preallocation.request_history_multiplier")
	private float preAllocationRequestHistoryMultiplier = -1.0F;

	private ResourceSpec preAllocationMinBound = null;
	private ResourceSpec preAllocationMaxBound = null;
	private NodeAllocatorConfig nodeAllocator = null;
	private NodePoolScalerConfig poolscaler = null;
	private Collection<Label> labels = new ArrayList<Label>();
	

	/* ========== Getters and Setters ========== */

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		ConfigHelpers.check(name, "Provider name is invalid!");
		
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		ConfigHelpers.check(name, "Provider type is invalid!");
		if (!NODE_ALLOCATOR_TYPES.containsKey(type))
			throw new ConfigException("Unknown provider type: " + type);
		
		this.type = type;
	}

	public String getDomain()
	{
		return this.domain;
	}

	public String getProtocol()
	{
		return protocol;
	}

	public NodeAllocatorConfig getNodeAllocatorConfig()
	{
		return nodeAllocator;
	}

	public void setNodeAllocatorConfig(NodeAllocatorConfig allocator)
	{
		if (allocator == null)
			throw new ConfigException("Node allocator is invalid!");
		
		this.nodeAllocator = allocator;
	}

	public NodePoolScalerConfig getPoolScalerConfig()
	{
		return poolscaler;
	}

	public void setPoolScalerConfig(NodePoolScalerConfig poolscaler)
	{
		if (poolscaler == null)
			throw new ConfigException("Pool scaler is invalid!");
		
		this.poolscaler = poolscaler;
	}

	public long getDeferredAllocationsGcInterval()
	{
		return deferredAllocationsGcInterval;
	}

	public void setDeferredAllocationsGcInterval(long interval)
	{
		ConfigHelpers.check(interval, 0L, Long.MAX_VALUE, "Deferred allocations GC interval is invalid!");
		
		this.deferredAllocationsGcInterval = interval;
	}
	
	public void setDeferredAllocationsGcInterval(long interval, TimeUnit unit)
	{
		this.setDeferredAllocationsGcInterval(unit.toMillis(interval));
	}
	
	public long getRequestHistoryUpdateInterval()
	{
		return reqHistoryUpdateInterval;
	}
	
	public void setRequestHistoryUpdateInterval(long interval)
	{
		ConfigHelpers.check(interval, 0L, Long.MAX_VALUE, "Update interval for request history is invalid!");
		
		this.reqHistoryUpdateInterval = interval;
	}
	
	public void setRequestHistoryUpdateInterval(long interval, TimeUnit unit)
	{
		this.setRequestHistoryUpdateInterval(unit.toMillis(interval));
	}

	public long getRequestHistoryMaxRequestAge()
	{
		return reqHistoryMaxRequestAge;
	}

	public void setRequestHistoryMaxRequestAge(long age)
	{
		ConfigHelpers.check(age, 0L, Long.MAX_VALUE, "Max. request age for request history is invalid!");
		
		this.reqHistoryMaxRequestAge = age;
	}
	
	public void setRequestHistoryMaxRequestAge(long age, TimeUnit unit)
	{
		this.setRequestHistoryMaxRequestAge(unit.toMillis(age));
	}

	public int getRequestHistoryMaxNumRequests()
	{
		return reqHistoryMaxNumRequests;
	}

	public void setRequestHistoryMaxNumRequests(int number)
	{
		ConfigHelpers.check(number, 0, Integer.MAX_VALUE, "Max. number of requests for request history is invalid!");
		
		this.reqHistoryMaxNumRequests = number;
	}

	public float getPreAllocationRequestHistoryMultiplier()
	{
		return preAllocationRequestHistoryMultiplier;
	}

	public void setPreAllocationRequestHistoryMultiplier(float multiplier)
	{
		final String message = "Request history multiplier for pre-allocation is invalid!";
		ConfigHelpers.check(multiplier, 0.0F, Float.MAX_VALUE, message);
		
		this.preAllocationRequestHistoryMultiplier = multiplier;
	}

	public ResourceSpec getPreAllocationMinBound()
	{
		return preAllocationMinBound;
	}

	public void setPreAllocationMinBound(ResourceSpec bound)
	{
		ConfigHelpers.check(bound, "Min. bound for pre-allocation is invalid!");
		
		this.preAllocationMinBound = bound;
	}

	public ResourceSpec getPreAllocationMaxBound()
	{
		return preAllocationMaxBound;
	}

	public void setPreAllocationMaxBound(ResourceSpec bound)
	{
		ConfigHelpers.check(bound, "Max. bound for pre-allocation is invalid!");
		
		this.preAllocationMaxBound = bound;
	}

	public Collection<Label> getLabels()
	{
		return labels;
	}

	public void setLabels(Collection<Label> labels)
	{
		if (labels == null)
			throw new ConfigException("List of provider labels is invalid!");

		// The collection can be empty!
		this.labels = labels;
	}

	public boolean hasHomogeneousNodes()
	{
		return nodeAllocator.hasHomogeneousNodes();
	}

	/* ========== Initialization ========== */

	@Override
	public void load(Configuration config) throws ConfigException
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);

		// Configure node allocator for this instance
		{
			ConfigHelpers.check(type, "Type of node allocator is missing!");
			Supplier<NodeAllocatorConfig> constructor = NODE_ALLOCATOR_TYPES.get(type);
			if (constructor == null)
				throw new ConfigException("Unknown type of node allocator specified: " + type);
			
			nodeAllocator = constructor.get();
			nodeAllocator.load(ConfigHelpers.filter(config, "node_allocator."));
		}
		
		// Configure pool scaler for this instance
		{
			Supplier<NodePoolScalerConfig> constructor = POOLSCALER_TYPES.get(type);
			if (constructor == null)
				throw new ConfigException("Pool scaler type not found for node allocator: " + type);
			
			poolscaler = constructor.get();
			poolscaler.load(ConfigHelpers.filter(config, "poolscaler."));
		}
		
		// Configure min. bound for pre-allocation
		{
			Configuration newconfig = ConfigHelpers.filter(config, "preallocation.min_bound.");
			preAllocationMinBound = ConfigHelpers.toResourceSpec(newconfig);
		}
		
		// Configure max. bound for pre-allocation
		{
			Configuration newconfig = ConfigHelpers.filter(config, "preallocation.max_bound.");
			preAllocationMaxBound = ConfigHelpers.toResourceSpec(newconfig);
		}

		// Configure labels
		{
			labels.clear();

			final Configuration subconfig = ConfigHelpers.filter(config, "labels.");
			final Map<String, String> properties = subconfig.getProperties();
			properties.forEach((key, value) -> labels.add(new Label(key, value)));
		}
	}

	@Override
	public void validate() throws ConfigException
	{
		this.valid = false;
		
		// Re-check the arguments...
		this.setName(name);
		this.setType(type);
		this.setDeferredAllocationsGcInterval(deferredAllocationsGcInterval);
		this.setRequestHistoryUpdateInterval(reqHistoryUpdateInterval);
		this.setRequestHistoryMaxRequestAge(reqHistoryMaxRequestAge);
		this.setRequestHistoryMaxNumRequests(reqHistoryMaxNumRequests);
		this.setPreAllocationRequestHistoryMultiplier(preAllocationRequestHistoryMultiplier);
		this.setPreAllocationMinBound(preAllocationMinBound);
		this.setPreAllocationMaxBound(preAllocationMaxBound);
		this.setNodeAllocatorConfig(nodeAllocator);
		this.setPoolScalerConfig(poolscaler);
		this.setLabels(labels);
		
		nodeAllocator.validate();
		poolscaler.validate();
		
		this.valid = true;
	}
	
	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.NAME, () -> json.write(DumpFields.NAME, name));
			dumper.add(DumpFields.TYPE, () -> json.write(DumpFields.TYPE, type));
			dumper.add(DumpFields.DEFERRED_ALLOCATIONS_GC_INTERVAL, () -> {
				json.write(DumpFields.DEFERRED_ALLOCATIONS_GC_INTERVAL,
						DumpHelpers.toDurationString(deferredAllocationsGcInterval));
			});
			
			dumper.add(DumpFields.LABELS, () -> {
				json.writeStartObject(DumpFields.LABELS);
				labels.forEach((label) -> json.write(label.getKey(), label.getValue()));
				json.writeEnd();
			});
			
			dumper.add(DumpFields.REQUEST_HISTORY, () -> {
				json.writeStartObject(DumpFields.REQUEST_HISTORY);
				json.write("update_interval", DumpHelpers.toDurationString(reqHistoryUpdateInterval));
				json.write("max_request_age", DumpHelpers.toDurationString(reqHistoryMaxRequestAge));
				json.write("max_num_requests", reqHistoryMaxNumRequests);
				json.writeEnd();
			});
			
			dumper.add(DumpFields.PREALLOCATION, () -> {
				json.writeStartObject(DumpFields.PREALLOCATION);
				json.write("request_history_multiplier", preAllocationRequestHistoryMultiplier);
				DumpHelpers.write(json, "min_bound", preAllocationMinBound);
				DumpHelpers.write(json, "max_bound", preAllocationMaxBound);
				json.writeEnd();
			});
			
			final int subflags = DumpFlags.set(flags, DumpFlags.INLINED);
			
			dumper.add(DumpFields.NODE_ALLOCATOR, () -> {
				json.writeStartObject(DumpFields.NODE_ALLOCATOR);
				nodeAllocator.dump(json, dconf, subflags);
				json.writeEnd();
			});
			
			dumper.add(DumpFields.POOLSCALER, () -> {
				json.writeStartObject(DumpFields.POOLSCALER);
				poolscaler.dump(json, dconf, subflags);
				json.writeEnd();
			});
			
			dumper.run();
		});
		
		trigger.run();
	}

	private static class DumpFields
	{
		private static final String NAME             = "name";
		private static final String TYPE             = "type";
		private static final String LABELS           = "labels";
		private static final String REQUEST_HISTORY  = "request_history";
		private static final String PREALLOCATION    = "preallocation";
		private static final String NODE_ALLOCATOR   = "node_allocator";
		private static final String POOLSCALER       = "poolscaler";
		
		private static final String DEFERRED_ALLOCATIONS_GC_INTERVAL
		                                             = "deferred_allocations_gc_interval";
	}
}
