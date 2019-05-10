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

import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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


public class NodeAllocatorConfigJCLOUDS extends NodeAllocatorConfig
{
	private ProviderConfig provider = null;

	@Config("security_group_name")
	private String securityGroupName = null;

	@Config("node_group_name")
	private String nodeGroupName = null;

	@Config("node_name_prefix")
	private String nodeNamePrefix = null;

	@Config("vm.network_id")
	private String vmNetworkId = null;

	@Config("vm.hardware_id")
	private String vmHardwareId = null;

	@Config("vm.image_id")
	private String vmImageId = null;

	@Config("vm.boot_poll_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long vmBootPollInterval = -1L;

	@Config("vm.boot_poll_interval_delta")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long vmBootPollIntervalDelta = -1L;

	@Config("vm.max_num_boot_polls")
	private int vmMaxNumBootPolls = -1;


	/* ========== Getters and Setters ========== */

	public String getProviderType()
	{
		return provider.getType();
	}

	public ProviderConfig getProviderConfig()
	{
		return provider;
	}

	public void setProviderConfig(ProviderConfig provider)
	{
		this.provider = provider;
	}

	public String getSecurityGroupName()
	{
		return securityGroupName;
	}

	public void setSecurityGroupName(String name)
	{
		ConfigHelpers.check(name, "Security group name is invalid!");

		this.securityGroupName = name;
	}

	public String getNodeGroupName()
	{
		return nodeGroupName;
	}

	public void setNodeGroupName(String name)
	{
		ConfigHelpers.check(name, "Node group name is invalid!");

		this.nodeGroupName = name;
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

	public String getVmNetworkId()
	{
		return vmNetworkId;
	}

	public void setVmNetworkId(String id)
	{
		ConfigHelpers.check(id, "Network ID is invalid!");

		this.vmNetworkId = id;
	}

	public String getVmHardwareId()
	{
		return vmHardwareId;
	}

	public void setVmHardwareId(String id)
	{
		ConfigHelpers.check(id, "Hardware ID is invalid!");

		this.vmHardwareId = id;
	}

	public String getVmImageId()
	{
		return vmImageId;
	}

	public void setVmImageId(String id)
	{
		ConfigHelpers.check(id, "Image ID is invalid!");

		this.vmImageId = id;
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

	@Override
	public boolean hasHomogeneousNodes()
	{
		return true;
	}


	/* ========== Provider Configs ========== */

	public static abstract class ProviderConfig
	{
		private final String type;


		protected ProviderConfig(String type)
		{
			this.type = type;
		}

		public String getType()
		{
			return type;
		}

		public <T> T as(Class<T> clazz)
		{
			return clazz.cast(this);
		}

		public abstract void load(Configuration config) throws ConfigException;

		public abstract void validate() throws ConfigException;

		public abstract JsonObject dump();

		public static ProviderConfig create(Configuration config)
		{
			ProviderConfig provider = null;

			final String type = config.get("type");
			switch (type) {
				case ProviderConfigOPENSTACK.TYPE:
					provider = new ProviderConfigOPENSTACK();
					break;

				default:
					throw new ConfigException("Unknown provider-type: " + type);
			}

			provider.load(config);
			return provider;
		}
	}

	public static class ProviderConfigOPENSTACK extends ProviderConfig
	{
		public static final String TYPE = "openstack-nova";

		@Config("auth.endpoint")
		private String authEndpoint = null;

		@Config("auth.api_version")
		private String authApiVersion = null;

		@Config("auth.project_name")
		private String authProjectName = null;

		@Config("auth.user")
		private String authUser = null;

		@Config("auth.password")
		private String authPassword= null;


		public ProviderConfigOPENSTACK()
		{
			super(TYPE);
		}

		public String getAuthEndpoint()
		{
			return authEndpoint;
		}

		public void setAuthEndpoint(String endpoint)
		{
			ConfigHelpers.check(endpoint, "Provider's auth-endpoint is invalid!");

			this.authEndpoint = endpoint;
		}

		public String getAuthApiVersion()
		{
			return authApiVersion;
		}

		public void setAuthApiVersion(String version)
		{
			ConfigHelpers.check(version, "Provider's auth-api-version is invalid!");

			this.authApiVersion = version;
		}

		public String getAuthProjectName()
		{
			return authProjectName;
		}

		public void setAuthProjectName(String project)
		{
			ConfigHelpers.check(project, "Provider's auth-project is invalid!");

			this.authProjectName = project;
		}

		public String getAuthUser()
		{
			return authUser;
		}

		public void setAuthUser(String user)
		{
			ConfigHelpers.check(user, "Provider's auth-user is invalid!");

			this.authUser = user;
		}

		public String getAuthPassword()
		{
			return authPassword;
		}

		public void setAuthPassword(String password)
		{
			ConfigHelpers.check(password, "Provider's auth-password is invalid!");

			this.authPassword = password;
		}

		@Override
		public void load(Configuration config) throws ConfigException
		{
			// Configure annotated members of this instance
			ConfigHelpers.configure(this, config);
		}

		@Override
		public void validate() throws ConfigException
		{
			// Re-check the arguments...
			this.setAuthEndpoint(authEndpoint);
			this.setAuthApiVersion(authApiVersion);
			this.setAuthProjectName(authProjectName);
			this.setAuthUser(authUser);
			this.setAuthPassword(authPassword);
		}

		@Override
		public JsonObject dump()
		{
			final JsonObjectBuilder auth = Json.createObjectBuilder()
					.add("endpoint", authEndpoint)
					.add("api_version", authApiVersion);

			return Json.createObjectBuilder()
					.add(DumpFields.TYPE, this.getType())
					.add(DumpFields.AUTH, auth.build())
					.build();
		}

		private static class DumpFields
		{
			private static final String TYPE  = "type";
			private static final String AUTH  = "auth";
		}
	}


	/* ========== Initialization ========== */

	@Override
	public void load(Configuration config) throws ConfigException
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);

		this.provider = ProviderConfig.create(ConfigHelpers.filter(config, "provider."));
	}

	@Override
	public void validate() throws ConfigException
	{
		this.valid = false;

		super.validate();

		// Re-check the arguments...
		this.provider.validate();
		this.setNodeGroupName(nodeGroupName);
		this.setNodeNamePrefix(nodeNamePrefix);
		this.setSecurityGroupName(securityGroupName);
		this.setVmNetworkId(vmNetworkId);
		this.setVmHardwareId(vmHardwareId);
		this.setVmImageId(vmImageId);
		this.setVmBootPollInterval(vmBootPollInterval);
		this.setVmBootPollIntervalDelta(vmBootPollIntervalDelta);
		this.setVmMaxNumBootPolls(vmMaxNumBootPolls);

		this.valid = true;
	}

	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.PROVIDER, () -> json.write(DumpFields.PROVIDER, provider.dump()));
			dumper.add(DumpFields.SECURITY_GROUP, () -> json.write(DumpFields.SECURITY_GROUP, securityGroupName));
			dumper.add(DumpFields.NODE_GROUP_NAME, () -> json.write(DumpFields.NODE_GROUP_NAME, nodeGroupName));
			dumper.add(DumpFields.NODE_NAME_PREFIX, () -> json.write(DumpFields.NODE_NAME_PREFIX, nodeNamePrefix));

			dumper.add(DumpFields.VM, () -> {
				json.writeStartObject(DumpFields.VM)
						.write("network_id", vmNetworkId)
						.write("hardware_id", vmHardwareId)
						.write("image_id", vmImageId)
						.write("boot_poll_interval", DumpHelpers.toDurationString(vmBootPollInterval))
						.write("boot_poll_interval_delta", DumpHelpers.toDurationString(vmBootPollIntervalDelta))
						.write("max_num_boot_polls", vmMaxNumBootPolls)
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
		private static final String PROVIDER           = "provider";
		private static final String SECURITY_GROUP     = "security_group_name";
		private static final String NODE_GROUP_NAME    = "node_group_name";
		private static final String NODE_NAME_PREFIX   = "node_name_prefix";
		private static final String VM                 = "vm";
	}
}
