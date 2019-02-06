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

package de.bwl.bwfla.metadata.oai.harvester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.bwl.bwfla.metadata.oai.harvester.config.BackendConfig;
import de.bwl.bwfla.metadata.oai.harvester.config.HarvesterConfig;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class HarvesterRegistry
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	private Client http;
	private HarvesterConfig config;
	private Map<String, HarvesterBackend> harvesters;

	public void add(BackendConfig backend)
	{
		final String name = backend.getName();
		if (harvesters.put(name, new HarvesterBackend(backend, http)) == null)
			log.info("Harvester backend '" + name + "' added");
		else log.info("Harvester backend '" + name + "' updated");
	}

	public HarvesterBackend lookup(String name)
	{
		return harvesters.get(name);
	}

	public boolean remove(String name)
	{
		if (harvesters.remove(name) == null)
			return false;

		log.info("Harvester backend '" + name + "' removed");
		return true;
	}

	public Collection<String> list()
	{
		return harvesters.keySet();
	}


	// ========== Internal Helpers ==============================

	@PostConstruct
	private void initialize()
	{
		this.http = ClientBuilder.newClient();
		this.config = new HarvesterConfig();
		this.harvesters = new ConcurrentHashMap<>();

		try {
			config.load(ConfigurationProvider.getConfiguration());
			for (BackendConfig backend : config.getBackendConfigs())
				this.add(backend);
		}
		catch (Exception error) {
			throw new IllegalStateException("Initializing metadata-harvester failed!", error);
		}

		this.restore();
	}

	@PreDestroy
	private void destroy()
	{
		http.close();
		this.save();
	}

	private void save()
	{
		final Path statepath = this.getStateDumpPath();

		log.info("Saving harvester's state to file...");
		try (final Writer writer = Files.newBufferedWriter(statepath)) {
			final Collection<HarvesterDescription> descriptions = harvesters.values().stream()
					.map((harvester) -> new HarvesterDescription(harvester))
					.collect(Collectors.toList());

			final ObjectMapper mapper = new ObjectMapper()
					.enable(SerializationFeature.INDENT_OUTPUT);

			mapper.writeValue(writer, descriptions);
		}
		catch (Exception error) {
			throw new IllegalStateException("Saving harvester's state failed!", error);
		}

		log.info("Harvester's state saved to: " + statepath.toString());
	}

	private void restore()
	{
		final Path statepath = this.getStateDumpPath();
		if (!Files.exists(statepath))
			return;   // Nothing to restore!

		log.info("Restoring harvester's state from: " + statepath.toString());
		try (final Reader input = Files.newBufferedReader(statepath)) {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.reader(HarvesterDescription.class)
					.readValues(input)
					.forEachRemaining((object) -> {
						final HarvesterDescription description = (HarvesterDescription) object;
						final HarvesterBackend backend = new HarvesterBackend(description, http);
						harvesters.put(backend.getName(), backend);

						log.info("Harvester's backend state restored: " + backend.getName());
					});
		}
		catch (Exception error) {
			throw new IllegalStateException("Restoring harvester's state failed!", error);
		}

		log.info("Harvester's state restored successfully");
	}

	private Path getStateDumpPath()
	{
		final String datadir = ConfigurationProvider.getConfiguration()
				.get("commonconf.serverdatadir");

		return Paths.get(datadir, "harvesters.json");
	}
}
