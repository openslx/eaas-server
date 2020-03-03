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

package de.bwl.bwfla.metadata.oai.provider;

import org.dspace.xoai.dataprovider.DataProvider;
import org.dspace.xoai.dataprovider.model.Context;
import org.dspace.xoai.dataprovider.repository.Repository;
import org.dspace.xoai.dataprovider.repository.RepositoryConfiguration;
import org.dspace.xoai.model.oaipmh.DeletedRecord;
import org.dspace.xoai.model.oaipmh.Granularity;
import de.bwl.bwfla.metadata.repository.client.MetaDataRepository;
import de.bwl.bwfla.metadata.oai.provider.config.BackendConfig;
import de.bwl.bwfla.metadata.oai.provider.config.ProviderConfig;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.transform.Transformer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;


@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ProviderRegistry
{
	private final Logger log = Logger.getLogger(ProviderRegistry.class.getName());

	private ProviderConfig config;
	private Context context;
	private Client http;

	private Map<String, DataProvider> providers;


	public DataProvider lookup(String name)
	{
		return providers.get(name);
	}

	public Collection<String> list()
	{
		return providers.keySet();
	}


	// ========== Internal Helpers ==============================

	@PostConstruct
	private void initialize()
	{
		this.config = new ProviderConfig();
		this.context = ProviderRegistry.newContext(log);
		this.http = ClientBuilder.newClient();
		this.providers = new LinkedHashMap<>();

		try {
			config.load(ConfigurationProvider.getConfiguration());
			for (BackendConfig backend : config.getBackendConfigs()) {
				final String name = backend.getName();
				final String baseurl = config.getBaseUrl();
				log.info("Preparing provider backend '" + name + "'...");

				final Repository repository = ProviderRegistry.newRepository(backend, baseurl, http, config.getSecret(), log);
				providers.put(name, new DataProvider(context, repository));
			}
		}
		catch (Exception error) {
			throw new IllegalStateException("Initializing metadata-provider failed!", error);
		}
	}

	@PreDestroy
	private void destroy()
	{
		http.close();
	}

	private static Context newContext(Logger log)
	{
		final Context context = new Context();

		// Supported metadata-formats
		{
			final String[] formats = {
					ProviderConfig.getMetaDataFormat()  // EaaS tech. metadata format
			};

			final Transformer transformer = ProviderConfig.getMetaDataTransformer();
			for (String format : formats)
				context.withMetadataFormat(format, transformer);
		}

		return context;
	}

	private static Repository newRepository(BackendConfig config, String baseurl, Client http, String secret, Logger log)
	{
		final Repository repository = new Repository();

		// Setup repository's config
		{
			final BackendConfig.IdentityConfig identity = config.getIdentityConfig();
			final BackendConfig.ResponseLimitsConfig limits = config.getResponseLimitsConfig();
			final RepositoryConfiguration repoconfig = new RepositoryConfiguration()
					.withDefaults()
					.withMaxListIdentifiers(limits.getMaxNumIdentifiers())
					.withMaxListRecords(limits.getMaxNumRecords())
					.withMaxListSets(limits.getMaxNumSets())
					.withBaseUrl(baseurl + "/" + config.getName())
					.withRepositoryName(identity.getRepositoryName())
					.withAdminEmail(identity.getAdminEmail())
					.withEarliestDate(identity.getEarliestDate())
					.withGranularity(Granularity.Second)
					.withDeleteMethod(DeletedRecord.NO);

			repository.withConfiguration(repoconfig);
		}

		// Setup set and item repository sources
		{
			final WebTarget endpoint = http.target(config.getSourceConfig().getBaseUrl());
			final MetaDataRepository mdrepo = new MetaDataRepository(endpoint, secret);

			repository.withSetRepository(new SetRepository(mdrepo));
			repository.withItemRepository(new ItemRepository(mdrepo));
		}

		return repository;
	}
}
