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

package de.bwl.bwfla.metadata.oai.harvester.config;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.metadata.oai.common.config.BaseConfig;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class HarvesterConfig extends BaseConfig
{
	public static final String BASE_KEY_PREFIX = "metadata_harvester.";

	private String baseurl;

	private List<BackendConfig> backends = new ArrayList<BackendConfig>();


	// ========== Getters and Setters ==============================

	public String getBaseUrl()
	{
		return baseurl;
	}

	@Config("base_url")
	public void setBaseUrl(String url)
	{
		ConfigHelpers.check(url, "Base URL is invalid!");
		this.baseurl = url;
	}

	public List<BackendConfig> getBackendConfigs()
	{
		return backends;
	}

	public void setBackendConfigs(List<BackendConfig> configs)
	{
		ConfigHelpers.check(configs, "List of metadata-harvester backends is invalid!");
		this.backends = configs;
	}


	// ========== Initialization ==============================

	@Override
	public void load(Configuration config) throws ConfigException
	{
		final Logger log = Logger.getLogger(this.getClass().getName());
		log.info("Loading metadata-harvester's configuration...");

		config = ConfigHelpers.filter(config, BASE_KEY_PREFIX);

		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);

		// Configure backends for this instance
		{
			backends.clear();

			while (true) {
				// Find out the type of configured backend
				final String fprefix = ConfigHelpers.toListKey("backends", backends.size(), ".");
				final Configuration backendConfig = ConfigHelpers.filter(config, fprefix);
				final String name = backendConfig.get("name");
				if (name == null)
					break;  // No more backends found!

				// Configure next backend
				BackendConfig backend = new BackendConfig();
				backend.load(backendConfig);
				backends.add(backend);
			}

			log.info("Loaded " + backends.size() + " harvester-backend configuration(s)");
		}
	}
}
