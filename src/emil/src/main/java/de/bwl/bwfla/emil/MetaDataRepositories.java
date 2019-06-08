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
import de.bwl.bwfla.emil.datatypes.SoftwareCollection;
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
	private EmilEnvironmentRepository environmentRepository = null;

	@Inject
	private EmilSoftwareData softwareData = null;


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
			final String name = repo.get("name");
			if (name == null)
				break;  // No more repositories configured!

			if (sources.lookup(name) != null)
				throw new ConfigException("Source with name '" + name + "' already registered!");

			if (sinks.lookup(name) != null)
				throw new ConfigException("Sink with name '" + name + "' already registered!");

			final String type = repo.get("type");
			final String mode = repo.getOrDefault("mode", AccessMode.READ_WRITE);
			switch (type) {
				case RepoType.ENVIRONMENTS:
					this.registerEnvironmentsRepository(name, mode, sources, sinks);
					break;

				case RepoType.IMAGES:
					this.registerImagesRepository(name, mode, sources, sinks);
					break;

				case RepoType.SOFTWARE:
					this.registerSoftwareRepository(name, mode, sources, sinks);
					break;

				default:
					throw new ConfigException("Unknown repository type: " + type);
			}

			log.info("--> " + name + " (type: " + type + ", mode: " + mode.toLowerCase() + ") registered");
			++numrepos;
		}

		log.info(numrepos + " metadata-repositories registered");
	}

	public void registerImagesRepository(String name, String mode, MetaDataSourceRegistry sources, MetaDataSinkRegistry sinks)
	{
		if (mode.equalsIgnoreCase(AccessMode.READ_ONLY) || mode.equalsIgnoreCase(AccessMode.READ_WRITE))
			sources.register(name, MetaDataSources.images("public", envdb, executor));

		if (mode.equalsIgnoreCase(AccessMode.WRITE_ONLY) || mode.equalsIgnoreCase(AccessMode.READ_WRITE))
			sinks.register(name, MetaDataSinks.images("remote", envdb));
	}

	public void registerEnvironmentsRepository(String name, String mode, MetaDataSourceRegistry sources, MetaDataSinkRegistry sinks)
	{
		if (mode.equalsIgnoreCase(AccessMode.READ_ONLY) || mode.equalsIgnoreCase(AccessMode.READ_WRITE))
			sources.register(name, MetaDataSources.environments(environmentRepository, executor));

		if (mode.equalsIgnoreCase(AccessMode.WRITE_ONLY) || mode.equalsIgnoreCase(AccessMode.READ_WRITE))
			sinks.register(name, MetaDataSinks.environments(environmentRepository));
	}

	public void registerSoftwareRepository(String name, String mode, MetaDataSourceRegistry sources, MetaDataSinkRegistry sinks)
	{
		if (mode.equalsIgnoreCase(AccessMode.READ_ONLY) || mode.equalsIgnoreCase(AccessMode.READ_WRITE))
			sources.register(name, MetaDataSources.software(softwareData, executor));

		if (mode.equalsIgnoreCase(AccessMode.WRITE_ONLY) || mode.equalsIgnoreCase(AccessMode.READ_WRITE))
			sinks.register(name, MetaDataSinks.software(softwareData));
	}


	private static final class RepoType
	{
		private static final String ENVIRONMENTS  = "environments";
		private static final String IMAGES        = "images";
		private static final String SOFTWARE      = "software";
	}

	private static final class AccessMode
	{
		private static final String READ_ONLY   = "r";
		private static final String WRITE_ONLY  = "w";
		private static final String READ_WRITE  = "rw";
	}
}
