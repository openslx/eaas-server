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

package de.bwl.bwfla.eaas.cluster.tenant;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.database.document.DocumentDatabaseConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.JsonUtils;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.exception.AllocationFailureException;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class TenantManager
{
	private final Logger log = Logger.getLogger("TENANT-MANAGER");

	private DocumentCollection<TenantConfig> collection;
	private Map<String, Tenant> tenants;


	public Collection<String> list() throws BWFLAException
	{
		final List<String> names = new ArrayList<>((int) collection.count());
		try (var entries = collection.list()) {
			for (var entry : entries)
				names.add(entry.getName());
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Listing tenants failed!", error);
			throw new BWFLAException(error);
		}

		return names;
	}

	public TenantManager add(TenantConfig config) throws BWFLAException
	{
		log.info("Adding new tenant: " + config.getName());
		this.store(config);
		return this;
	}

	public boolean update(String tid, ResourceSpec limits) throws BWFLAException
	{
		log.info("Updating tenant: " + tid);

		final TenantConfig config = this.load(tid);
		if (config == null)
			return false;

		config.setQuotaLimits(limits);
		this.store(config);
		return true;
	}

	public Tenant.Quota quota(String tid) throws BWFLAException
	{
		final Wrapper<Tenant.Quota> result = new Wrapper<>(null);

		final BiFunction<String, Tenant, Tenant> functor = (name, tenant) -> {
			if (tenant != null) {
				// atomically copy cached quota...
				result.set(new Tenant.Quota(tenant.getQuota()));
			}
			else {
				// load quota from DB (skip caching)...
				final TenantConfig config = this.load(name);
				if (config != null)
					result.set(new Tenant.Quota(config.getQuotaLimits()));
			}

			return tenant;
		};

		this.apply(tid, functor);
		return result.get();
	}

	public boolean remove(String tid) throws BWFLAException
	{
		log.info("Removing tenant: " + tid);

		final var ok = collection.delete(TenantConfig.filter(tid));
		tenants.remove(tid);
		return ok;
	}

	public boolean allocate(String tid, ResourceSpec spec) throws AllocationFailureException
	{
		final Wrapper<Boolean> result = new Wrapper<>(false);

		final BiFunction<String, Tenant, Tenant> functor = (name, tenant) -> {
			if (tenant == null) {
				// tenant's config is not yet cached, try to load it from DB!
				final var config = this.load(name);
				if (config == null)
					throw new IllegalArgumentException();

				tenant = new Tenant(config);
			}

			final var allocated = tenant.getQuota()
					.allocate(spec);

			result.set(allocated);
			return tenant;
		};

		this.apply(tid, functor);
		return result.get();
	}

	public void free(String tid, ResourceSpec spec) throws AllocationFailureException
	{
		final BiFunction<String, Tenant, Tenant> functor = (name, tenant) -> {
			if (tenant != null) {
				final var quota = tenant.getQuota();
				quota.free(spec);
			}

			return tenant;
		};

		this.apply(tid, functor);
	}


	// ========== Internal Stuff ====================

	@PostConstruct
	private void initialize()
	{
		this.collection = TenantManager.getTenantCollection(log);
		this.tenants = new ConcurrentHashMap<>();
	}

	/*
	private void restore()
	{
		final Path statepath = this.getStateDumpPath();
		if (!Files.exists(statepath))
			return;   // Nothing to restore!

		log.info("Restoring tenants from: " + statepath.toString());
		try {
			final List<TenantConfig> configs = JsonUtils.restore(statepath, new TypeReference<List<TenantConfig>>(){}, log);
			configs.forEach((config) -> this.add(config));
		}
		catch (Exception error) {
			throw new IllegalStateException("Restoring tenants failed!", error);
		}

		log.info(tenants.size() + " tenant(s) restored successfully");
	}

	private Path getStateDumpPath()
	{
		final String datadir = ConfigurationProvider.getConfiguration()
				.get("commonconf.serverdatadir");

		return Paths.get(datadir, "tenants.json");
	}
	 */

	private void store(TenantConfig config) throws BWFLAException
	{
		final var name = config.getName();
		final var filter = TenantConfig.filter(name);
		collection.replace(filter, config);

		// update cached entry...
		final BiFunction<String, Tenant, Tenant> updater = (unused, current) -> {
			current.getQuota()
					.setLimits(config.getQuotaLimits());

			return current;
		};

		tenants.computeIfPresent(name, updater);
	}

	private TenantConfig load(String tid)
	{
		try {
			return collection.lookup(TenantConfig.filter(tid));
		}
		catch (BWFLAException error) {
			log.log(Level.WARNING, "Loading tenant-config failed!", error);
			return null;
		}
	}

	private void apply(String tid, BiFunction<String, Tenant, Tenant> functor) throws AllocationFailureException
	{
		try {
			// atomically apply functor
			tenants.compute(tid, functor);
		}
		catch (Exception error) {
			final String message = (error instanceof IllegalArgumentException) ?
					"Unknown tenant ID: " + tid : "Updating tenant-config failed!";

			throw new AllocationFailureException(message, error);
		}
	}

	private static DocumentCollection<TenantConfig> getTenantCollection(Logger log)
	{
		final String dbname = ConfigurationProvider.getConfiguration()
				.get("commonconf.mongodb.dbname");

		final String cname = "tenants";

		log.info("Initializing collection: " + cname + " (" + dbname + ")");
		try {
			final DocumentCollection<TenantConfig> entries = DocumentDatabaseConnector.instance()
					.database(dbname)
					.collection(cname, TenantConfig.class);

			entries.index(TenantConfig.Fields.NAME);
			return entries;
		}
		catch (Exception error) {
			throw new RuntimeException(error);
		}
	}

	private static class Wrapper<T>
	{
		private T value;

		public Wrapper(T value)
		{
			this.value = value;
		}

		public void set(T value)
		{
			this.value = value;
		}

		public T get()
		{
			return value;
		}
	}
}
