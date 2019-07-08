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
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.WithPropertyConverter;

import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.config.util.DurationPropertyConverter;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;


public class NodeAllocatorConfigGCE extends NodeAllocatorConfig
{
	@Config("application_name")
	private String appName = null;

	@Config("project_id")
	private String projectId = null;

	@Config("zone_name")
	private String zoneName = null;

	@Config("network_name")
	private String networkName = null;

	@Config("node_name_prefix")
	private String nodeNamePrefix = null;

	@Config("credentials_file")
	private String serviceAccountCredentialsFile = null;

	@Config("vm.machine_type")
	private String vmType = null;

	@Config("vm.min_cpu_platform")
	private String vmMinCpuPlatform = null;

	@Config("vm.persistent_disk.type")
	private String vmPersistentDiskType = null;

	@Config("vm.persistent_disk.size")
	private long vmPersistentDiskSize = -1;

	@Config("vm.persistent_disk.image_url")
	private String vmPersistentDiskImageUrl = null;

	@Config("vm.boot_poll_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long vmBootPollInterval = -1L;

	@Config("vm.boot_poll_interval_delta")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long vmBootPollIntervalDelta = -1L;
	
	@Config("vm.max_num_boot_polls")
	private int vmMaxNumBootPolls = -1;
	
	/** Config: vm.accelerators */
	private List<AcceleratorConfig> vmAccelerators = new ArrayList<AcceleratorConfig>();

	@Config("api.poll_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long apiPollInterval = -1L;

	@Config("api.poll_interval_delta")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long apiPollIntervalDelta = -1L;

	@Config("api.retry_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long apiRetryInterval = -1L;

	@Config("api.retry_interval_delta")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long apiRetryIntervalDelta = -1L;

	@Config("api.max_num_retries")
	private int apiMaxNumRetries = -1;


	/* ========== Helper-Classes ========== */

	public static class AcceleratorConfig extends BaseConfig
	{
		@Config("type")
		private String type = null;

		@Config("count")
		private int count = -1;


		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			ConfigHelpers.check(type, "Accelerator type is invalid!");
			this.type = type;
		}

		public int getCount()
		{
			return count;
		}

		public void setCount(int count)
		{
			ConfigHelpers.check(count, 1, Integer.MAX_VALUE, "Number of accelerators is invalid!");
			this.count = count;
		}

		@Override
		public void load(Configuration config) throws ConfigException
		{
			ConfigHelpers.configure(this, config);
		}

		@Override
		public void validate() throws ConfigException
		{
			this.valid = false;

			// Re-check the arguments...
			this.setType(type);
			this.setCount(count);

			this.valid = true;
		}

		@Override
		public void dump(JsonGenerator json, DumpConfig dconf, int flags)
		{
			final DumpTrigger trigger = new DumpTrigger(dconf);
			trigger.setResourceDumpHandler(() -> {
				final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
				dumper.add("type", () -> json.write("type", type));
				dumper.add("count", () -> json.write("count", count));
				dumper.run();
			});

			trigger.run();
		}
	}


	/* ========== Getters and Setters ========== */

	public String getAppName()
	{
		return appName;
	}

	public void setAppName(String name)
	{
		ConfigHelpers.check(name, "Application name is invalid!");

		this.appName = name;
	}

	public String getProjectId()
	{
		return projectId;
	}

	public void setProjectId(String id)
	{
		ConfigHelpers.check(id, "Project ID is invalid!");

		this.projectId = id;
	}

	public String getZoneName()
	{
		return zoneName;
	}

	public void setZoneName(String name)
	{
		ConfigHelpers.check(name, "Zone name is invalid!");

		this.zoneName = name;
	}

	public String getNetworkName()
	{
		return networkName;
	}

	public void setNetworkName(String name)
	{
		ConfigHelpers.check(name, "Network name is invalid!");

		this.networkName = name;
	}

	public String getVmType()
	{
		return vmType;
	}

	public void setVmType(String type)
	{
		ConfigHelpers.check(type, "Machine type is invalid!");

		this.vmType = type;
	}

	public String getVmMinCpuPlatform()
	{
		return vmMinCpuPlatform;
	}

	public void setVmMinCpuPlatform(String platform)
	{
		ConfigHelpers.check(platform, "Machine's CPU platform is invalid!");

		this.vmMinCpuPlatform = platform;
	}

	public String getVmPersistentDiskType()
	{
		return vmPersistentDiskType;
	}

	public void setVmPersistentDiskType(String type)
	{
		ConfigHelpers.check(type, "Persistent disk type is invalid!");

		this.vmPersistentDiskType = type;
	}

	public long getVmPersistentDiskSize()
	{
		return vmPersistentDiskSize;
	}

	public void setVmPersistentDiskSize(long size)
	{
		ConfigHelpers.check(size, 1L, Long.MAX_VALUE, "Persistent disk size is invalid!");

		this.vmPersistentDiskSize = size;
	}

	public String getVmPersistentDiskImageUrl()
	{
		return vmPersistentDiskImageUrl;
	}

	public void setVmPersistentDiskImageUrl(String url)
	{
		ConfigHelpers.check(url, "URL for persistent disk image is invalid!");

		this.vmPersistentDiskImageUrl = url;
	}

	public long getVmBootPollInterval()
	{
		return vmBootPollInterval;
	}

	public void setVmBootPollInterval(long interval)
	{
		ConfigHelpers.check(interval, 0L, Long.MAX_VALUE, "Interval for VM boot polling is invalid!");

		this.vmBootPollInterval = interval;
	}

	public void setVmBootPollInterval(long interval, TimeUnit unit)
	{
		this.setVmBootPollInterval(unit.toMillis(interval));
	}

	public long getVmBootPollIntervalDelta()
	{
		return vmBootPollIntervalDelta;
	}

	public void setVmBootPollIntervalDelta(long delta)
	{
		ConfigHelpers.check(delta, 0L, Long.MAX_VALUE, "Interval delta for VM boot polling is invalid!");

		this.vmBootPollIntervalDelta = delta;
	}

	public void setVmBootPollIntervalDelta(long delta, TimeUnit unit)
	{
		this.setVmBootPollIntervalDelta(unit.toMillis(delta));
	}
	
	public int getVmMaxNumBootPolls()
	{
		return vmMaxNumBootPolls;
	}

	public void setVmMaxNumBootPolls(int number)
	{
		ConfigHelpers.check(number, 1, Integer.MAX_VALUE, "Max. number of boot polls is invalid!");

		this.vmMaxNumBootPolls = number;
	}

	public List<AcceleratorConfig> getVmAccelerators()
	{
		return vmAccelerators;
	}

	public void setVmAccelerators(List<AcceleratorConfig> accelerators)
	{
		if (accelerators == null)
			throw new ConfigException("List of VM accelerators is invalid!");

		// List can be empty!
		this.vmAccelerators = accelerators;
	}

	public String getServiceAccountCredentialsFile()
	{
		return serviceAccountCredentialsFile;
	}

	public void setServiceAccountCredentialsFile(String file)
	{
		ConfigHelpers.check(file, "Service account credentials-file is invalid!");

		this.serviceAccountCredentialsFile = file;
	}

	public String getNodeNamePrefix()
	{
		return nodeNamePrefix;
	}

	public void setNodeNamePrefix(String name)
	{
		ConfigHelpers.check(name, "Node name prefix is invalid!");

		this.nodeNamePrefix = name;
	}

	public long getApiPollInterval()
	{
		return apiPollInterval;
	}

	public void setApiPollInterval(long interval)
	{
		ConfigHelpers.check(interval, 0L, Long.MAX_VALUE, "Interval for API polling is invalid!");

		this.apiPollInterval = interval;
	}

	public void setApiPollInterval(long interval, TimeUnit unit)
	{
		this.setApiPollInterval(unit.toMillis(interval));
	}

	public long getApiPollIntervalDelta()
	{
		return apiPollIntervalDelta;
	}

	public void setApiPollIntervalDelta(long delta)
	{
		ConfigHelpers.check(delta, 0L, Long.MAX_VALUE, "Interval delta for API polling is invalid!");

		this.apiPollIntervalDelta = delta;
	}

	public void setApiPollIntervalDelta(long delta, TimeUnit unit)
	{
		this.setApiPollIntervalDelta(unit.toMillis(delta));
	}

	public long getApiRetryInterval()
	{
		return apiRetryInterval;
	}

	public void setApiRetryInterval(long interval)
	{
		ConfigHelpers.check(interval, 0L, Long.MAX_VALUE, "Interval for API retries is invalid!");

		this.apiRetryInterval = interval;
	}

	public void setApiRetryInterval(long interval, TimeUnit unit)
	{
		this.setApiRetryInterval(unit.toMillis(interval));
	}

	public long getApiRetryIntervalDelta()
	{
		return apiRetryIntervalDelta;
	}

	public void setApiRetryIntervalDelta(long delta)
	{
		ConfigHelpers.check(delta, 0L, Long.MAX_VALUE, "Interval delta for API retries is invalid!");

		this.apiRetryIntervalDelta = delta;
	}

	public void setApiRetryIntervalDelta(long delta, TimeUnit unit)
	{
		this.setApiRetryIntervalDelta(unit.toMillis(delta));
	}

	public int getApiMaxNumRetries()
	{
		return apiMaxNumRetries;
	}

	public void setApiMaxNumRetries(int number)
	{
		ConfigHelpers.check(number, 1, Integer.MAX_VALUE, "Max. number of API retries is invalid!");

		this.apiMaxNumRetries = number;
	}

	public boolean hasHomogeneousNodes()
	{
		return true;
	}


	/* ========== Initialization ========== */

	@Override
	public void load(Configuration config) throws ConfigException
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);
		
		// Configure VM accelerators...
		{
			vmAccelerators.clear();

			while (true) {
				final String prefix = ConfigHelpers.toListKey("vm.accelerators", vmAccelerators.size(), ".");
				final Configuration subconfig = ConfigHelpers.filter(config, prefix);
				if (subconfig.get("type") == null)
					break;

				// Configure next accelerator
				AcceleratorConfig accelerator = new AcceleratorConfig();
				accelerator.load(subconfig);
				vmAccelerators.add(accelerator);
			}
		}
	}

	@Override
	public void validate() throws ConfigException
	{
		this.valid = false;

		super.validate();

		// Re-check the arguments...
		this.setAppName(appName);
		this.setProjectId(projectId);
		this.setZoneName(zoneName);
		this.setNetworkName(networkName);
		this.setNodeNamePrefix(nodeNamePrefix);
		this.setServiceAccountCredentialsFile(serviceAccountCredentialsFile);
		this.setVmType(vmType);
		this.setVmPersistentDiskType(vmPersistentDiskType);
		this.setVmPersistentDiskSize(vmPersistentDiskSize);
		this.setVmPersistentDiskImageUrl(vmPersistentDiskImageUrl);
		this.setVmBootPollInterval(vmBootPollInterval);
		this.setVmBootPollIntervalDelta(vmBootPollIntervalDelta);
		this.setVmMaxNumBootPolls(vmMaxNumBootPolls);
		this.setVmAccelerators(vmAccelerators);
		this.setApiPollInterval(apiPollInterval);
		this.setApiPollIntervalDelta(apiPollIntervalDelta);
		this.setApiRetryInterval(apiRetryInterval);
		this.setApiRetryIntervalDelta(apiRetryIntervalDelta);
		this.setApiMaxNumRetries(apiMaxNumRetries);

		this.valid = true;
	}

	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.APPLICATION_NAME, () -> json.write(DumpFields.APPLICATION_NAME, appName));
			dumper.add(DumpFields.PROJECT_ID, () -> json.write(DumpFields.PROJECT_ID, projectId));
			dumper.add(DumpFields.ZONE_NAME, () -> json.write(DumpFields.ZONE_NAME, zoneName));
			dumper.add(DumpFields.NETWORK_NAME, () -> json.write(DumpFields.NETWORK_NAME, networkName));
			dumper.add(DumpFields.NODE_NAME_PREFIX, () -> json.write(DumpFields.NODE_NAME_PREFIX, nodeNamePrefix));
			dumper.add(DumpFields.CREDENTIALS_FILE, () -> json.write(DumpFields.CREDENTIALS_FILE, serviceAccountCredentialsFile));
			
			dumper.add(DumpFields.VM, () -> {
				json.writeStartObject(DumpFields.VM)
					.write("machine_type", vmType)
					.write("min_cpu_platform", vmMinCpuPlatform)
					.writeStartObject("persistent_disk")
						.write("type", vmPersistentDiskType)
						.write("size", vmPersistentDiskSize)
						.write("image_url", vmPersistentDiskImageUrl)
					.writeEnd()
					.writeStartArray("accelerators");
					for (AcceleratorConfig accelerator : vmAccelerators)
						accelerator.dump(json, dconf, 0);
					
					json.writeEnd()
					.write("boot_poll_interval", DumpHelpers.toDurationString(vmBootPollInterval))
					.write("boot_poll_interval_delta", DumpHelpers.toDurationString(vmBootPollIntervalDelta))
					.write("max_num_boot_polls", vmMaxNumBootPolls)
				.writeEnd();
			});
			
			dumper.add(DumpFields.API, () -> {
				json.writeStartObject(DumpFields.API)
					.write("poll_interval", DumpHelpers.toDurationString(apiPollInterval))
					.write("poll_interval_delta", DumpHelpers.toDurationString(apiPollIntervalDelta))
					.write("retry_interval", DumpHelpers.toDurationString(apiRetryInterval))
					.write("retry_interval_delta", DumpHelpers.toDurationString(apiRetryIntervalDelta))
					.write("max_num_retries", apiMaxNumRetries)
				.writeEnd();
			});
			
			final JsonObject object = super.dump();
			object.forEach((key, value) -> {
				dumper.add(key, () -> json.write(key, value));
			});
			
			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String APPLICATION_NAME  = "application_name";
		private static final String PROJECT_ID        = "project_id";
		private static final String ZONE_NAME         = "zone_name";
		private static final String NETWORK_NAME      = "network_name";
		private static final String NODE_NAME_PREFIX  = "node_name_prefix";
		private static final String CREDENTIALS_FILE  = "credentials_file";
		private static final String VM                = "vm";
		private static final String API               = "api";
	}
}
