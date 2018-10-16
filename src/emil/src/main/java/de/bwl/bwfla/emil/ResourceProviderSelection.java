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

package de.bwl.bwfla.emil;

import de.bwl.bwfla.eaas.cluster.config.BaseConfig;
import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.metadata.LabelSelectorParser;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.stream.JsonGenerator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@ApplicationScoped
public class ResourceProviderSelection
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	@Config(value = "name")
	private String name = "default";

	private final Map<String, List<String>> mapping = new LinkedHashMap<String, List<String>>();


	public String getName()
	{
		return name;
	}

	public List<String> getSelectors(String envid)
	{
		return mapping.get(envid);
	}


	/* =============== Internal Helpers =============== */

	@PostConstruct
	protected void initialize()
	{
		final Configuration config = ConfigurationProvider.getConfiguration();

		// Configure this resource provider selection, if possible...
		if (config.get("resource_provider_selection.name") != null) {
			try {
				this.load(ConfigHelpers.filter(config, "resource_provider_selection."));
				this.validate();
			}
			catch (Exception exception) {
				log.severe("Configuring resource provider selection failed!");
				throw exception;
			}
		}
		else {
			log.info("Disabling resource provider selection! No configuration found.");
		}
	}

	private void load(Configuration config)
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);

		log.info("Loading resource provider selection '" + name + "'...");

		mapping.clear();

		while (true) {
			// Parse next entry...
			final String userPrefix = ConfigHelpers.toListKey("entries", mapping.size(), ".");
			final Configuration userConfig = ConfigHelpers.filter(config, userPrefix);
			final String envid = userConfig.get("environment");
			if (envid == null)
				break;  // No more entries found!

			ResourceProviderSelector entry = new ResourceProviderSelector();
			entry.load(ConfigHelpers.filter(config, userPrefix));
			mapping.put(entry.getEnvironmentId(), entry.getSelectors());
		}

		log.info(mapping.size() + " resource provider selection entries loaded");
	}

	private void validate() throws ConfigException
	{
		ConfigHelpers.check(name, "Invalid name!");

		final LabelSelectorParser parser = new LabelSelectorParser();
		for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
			for (String selector : entry.getValue()) {
				try {
					parser.parse(selector);
				}
				catch (Exception error) {
					final String message = "Verifying selector '" + selector + "' for environment '"
							+ entry.getKey() + "' failed!";

					throw new ConfigException(message, error);
				}
			}
		}
	}

	private static class ResourceProviderSelector extends BaseConfig
	{
		@Config(value = "environment")
		private String envid;

		private List<String> selectors = new ArrayList<String>();

		public String getEnvironmentId()
		{
			return envid;
		}

		public List<String> getSelectors()
		{
			return selectors;
		}

		@Override
		public void load(Configuration config) throws ConfigException
		{
			ConfigHelpers.configure(this, config);
			selectors = ConfigHelpers.getAsList(config, "selectors");
		}

		@Override
		public void validate() throws ConfigException
		{
			ConfigHelpers.check(envid, "Environment ID is invalid!");

			try {
				final LabelSelectorParser parser = new LabelSelectorParser();
				for (String selector : selectors)
					parser.parse(selector);
			}
			catch (Exception error) {
				throw new ConfigException("Selectors are invalid!", error);
			}
		}

		@Override
		public void dump(JsonGenerator json, DumpConfig dconf, int flags)
		{
			throw new UnsupportedOperationException();
		}
	}
}
