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

import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.metadata.repository.IMetaDataRepositoryAPI;
import de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI;
import de.bwl.bwfla.metadata.repository.MetaDataSinkRegistry;
import de.bwl.bwfla.metadata.repository.MetaDataSourceRegistry;
import de.bwl.bwfla.metadata.repository.api.HttpDefs;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.concurrent.Executor;
import java.util.logging.Logger;


@ApplicationScoped
@Path("/metadata-repositories/{name}")
public class MetaDataRepositories implements IMetaDataRepositoryAPI
{
	@Resource(lookup = "java:jboss/ee/concurrency/executor/io")
	private Executor executor = null;

	@Inject
	private MetaDataRepositoryAPI mdrepo = null;

	@Inject
	private DatabaseEnvironmentsAdapter envdb = null;

	@Inject
	private EmilEnvironmentRepository environmentRepository;

	// ========== MetaDataRepository API =========================

	@Override
	@Path(HttpDefs.Paths.SETS)
	public de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI.Sets sets(@PathParam("name") String name)
	{
		return mdrepo.sets(name);
	}

	@Override
	@Path(HttpDefs.Paths.IDENTIFIERS)
	public de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI.ItemIdentifiers identifiers(@PathParam("name") String name)
	{
		return mdrepo.identifiers(name);
	}

	@Override
	@Path(HttpDefs.Paths.ITEMS)
	public de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI.Items items(@PathParam("name") String name)
	{
		return mdrepo.items(name);
	}


	// ========== Internal Helpers =========================

	@PostConstruct
	private void initialize()
	{
		final Configuration config = ConfigurationProvider.getConfiguration();
		final MetaDataSourceRegistry sources = mdrepo.sources();
		final MetaDataSinkRegistry sinks = mdrepo.sinks();

		final Logger log = Logger.getLogger(this.getClass().getName());
		log.info("Registering metadata-repositories...");

		int numrepos = 0;
		while (true) {
			final String prefix = ConfigHelpers.toListKey("metadata_repositories", numrepos, ".");
			final Configuration repo = ConfigHelpers.filter(config, prefix);
			final String type = repo.get("type");
			if (type == null)
				break;

			final String mode = repo.get("mode");
			final String name = repo.get("name");
			switch (type)
			{
				case "images":
					switch(mode)
					{
						case "R":
							sources.register(name, MetaDataSources.images("public", envdb, executor));
							break;
						case "W":
							sinks.register(name, MetaDataSinks.images("remote", envdb));
							break;
						default:
							sources.register(name, MetaDataSources.images("public", envdb, executor));
							sinks.register(name, MetaDataSinks.images("remote", envdb));
					}
					log.info("--> " + name + " (" + type + ") registered");
					break;

				case "environments":
					switch (mode) {
						case "R":
							sources.register(name, MetaDataSources.environments(environmentRepository, executor));
							break;
						case "W":
							sinks.register(name, MetaDataSinks.environments(environmentRepository));
							break;
						default:
							sources.register(name, MetaDataSources.environments(environmentRepository, executor));
							sinks.register(name, MetaDataSinks.environments(environmentRepository));
					}
					log.info("--> " + name + " (" + type + ") registered");
					break;

				default:
					throw new ConfigException("Unknown repository type: " + type);
			}
			++numrepos;
		}

		log.info(numrepos + " metadata-repositories registered");
	}
}
