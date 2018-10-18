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
import java.util.logging.Logger;

import javax.json.stream.JsonGenerator;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpFlags;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;


public class ClusterManagerConfig extends BaseConfig
{
	@Config("clustermanager.name")
	private String name = null;
	
	@Config("clustermanager.admin_api_access_token")
	private String adminApiAccessToken = null;

	private List<ResourceProviderConfig> providers = new ArrayList<ResourceProviderConfig>();


	/* ========== Getters and Setters ========== */

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		
		this.name = name;
	}

	public String getAdminApiAccessToken()
	{
		return adminApiAccessToken;
	}

	public void setAdminApiAccessToken(String token)
	{
		ConfigHelpers.check(token, "Admin-API access token is invalid!");

		this.adminApiAccessToken = token;
	}

	public List<ResourceProviderConfig> getResourceProviderConfigs()
	{
		return providers;
	}

	public void setResourceProviderConfigs(List<ResourceProviderConfig> providers)
	{
		ConfigHelpers.check(providers, "List of resource providers is invalid!");

		this.providers = providers;
	}


	/* ========== Initialization ========== */

	@Override
	public void load(Configuration config)
	{
		final Logger log = Logger.getLogger(this.getClass().getName());

		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);

		// Configure providers for this instance
		{
			providers.clear();
			
			while (true) {
				// Find out the type of configured provider
				final String userPrefix = ConfigHelpers.toListKey("clustermanager.providers", providers.size(), ".");
				final Configuration userConfig = ConfigHelpers.filter(config, userPrefix);
				final String type = userConfig.get("type");
				if (type == null)
					break;  // No more providers found!
				
				// Compute the combined view from user and default configurations
				final String defaultPrefix = "clustermanager.providers.defaults.";
				final Configuration defaultAllConfig = ConfigHelpers.filter(config, defaultPrefix + "all.");
				final Configuration defaultTypeConfig = ConfigHelpers.filter(config, defaultPrefix + type + ".");
				final Configuration combinedConfig = ConfigHelpers.combine(defaultAllConfig, defaultTypeConfig, userConfig);
				
				// Configure next provider
				ResourceProviderConfig provider = new ResourceProviderConfig();
				provider.load(combinedConfig);
				providers.add(provider);
			}
			
			log.info("Loaded " + providers.size() + " provider configuration(s)");
		}
	}

	@Override
	public void validate() throws ConfigException
	{
		this.valid = false;
		
		// Re-check the arguments...
		this.setName(name);
		this.setAdminApiAccessToken(adminApiAccessToken);
		this.setResourceProviderConfigs(providers);

		for (ResourceProviderConfig provider : providers)
			provider.validate();
		
		this.valid = true;
	}

	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.NAME, () -> json.write(DumpFields.NAME, name));
			dumper.add(DumpFields.PROVIDERS, () -> {
				final int subflags = DumpFlags.reset(flags, DumpFlags.INLINED);
				json.writeStartArray(DumpFields.PROVIDERS);
				for (ResourceProviderConfig provider : providers)
					provider.dump(json, dconf, subflags);
				
				json.writeEnd();
			});
			
			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String NAME       = "name";
		private static final String PROVIDERS  = "providers";
	}
}
