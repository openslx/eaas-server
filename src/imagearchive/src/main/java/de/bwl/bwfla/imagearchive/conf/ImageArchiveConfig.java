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

package de.bwl.bwfla.imagearchive.conf;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class ImageArchiveConfig extends BaseConfig
{
	private String defaultBackendName;

	private List<ImageArchiveBackendConfig> backends = new ArrayList<ImageArchiveBackendConfig>();


	/* ========== Getters and Setters ========== */

	public String getDefaultBackendName()
	{
		return defaultBackendName;
	}

	@Config("imagearchive.default_backend_name")
	public void setDefaultBackendName(String name)
	{
		ConfigHelpers.check(name, "Default backend-name is invalid!");
		this.defaultBackendName = name;
	}

	public List<ImageArchiveBackendConfig> getBackendConfigs()
	{
		return backends;
	}

	public void setBackendConfigs(List<ImageArchiveBackendConfig> backends)
	{
		ConfigHelpers.check(backends, "List of image-archive backends is invalid!");
		this.backends = backends;
	}


	/* ========== Initialization ========== */

	@Override
	public void load(Configuration config) throws ConfigException
	{
		final Logger log = Logger.getLogger(this.getClass().getName());
		log.info("Loading image-archive's configuration...");

		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);

		// Configure backends for this instance
		{
			backends.clear();

			while (true) {
				// Find out the type of configured provider
				final String userPrefix = ConfigHelpers.toListKey("imagearchive.backends", backends.size(), ".");
				final Configuration userConfig = ConfigHelpers.filter(config, userPrefix);
				final String type = userConfig.get("type");
				if (type == null)
					break;  // No more backends found!

				// Compute the combined view from user and default configurations
				final String defaultPrefix = "imagearchive.backends.defaults.";
				final Configuration defaultAllConfig = ConfigHelpers.filter(config, defaultPrefix + "all.");
				final Configuration defaultTypeConfig = ConfigHelpers.filter(config, defaultPrefix + type + ".");
				final Configuration combinedConfig = ConfigHelpers.combine(defaultAllConfig, defaultTypeConfig, userConfig);

				// Configure next backend
				ImageArchiveBackendConfig backend = new ImageArchiveBackendConfig();
				backend.load(combinedConfig);
				backends.add(backend);
			}

			log.info("Loaded " + backends.size() + " backend configuration(s)");
		}
	}
}
