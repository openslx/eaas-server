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
import de.bwl.bwfla.common.utils.JsonUtils;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.exception.AllocationFailureException;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
public class TenantManager
{
	private final Logger log = Logger.getLogger(TenantManager.class.getName());

	private final Map<String, Tenant> tenants;


	public TenantManager()
	{
		this.tenants = new ConcurrentHashMap<>();
	}

	public Collection<String> list()
	{
		return tenants.keySet();
	}

	public TenantManager add(TenantConfig config)
	{
		final String tid = config.getName();
		log.info("Adding new tenant: " + tid);
		tenants.put(tid, new Tenant(config));
		return this;
	}

	public TenantManager update(String tid, ResourceSpec limits) throws AllocationFailureException
	{
		log.info("Updating tenant: " + tid);

		final Tenant tenant = this.lookup(tid);
		synchronized (tenant) {
			tenant.getQuota()
					.setLimits(limits);
		}

		return this;
	}

	public Tenant.Quota quota(String tid) throws AllocationFailureException
	{
		final Tenant tenant = this.lookup(tid);
		synchronized (tenant) {
			return tenant.getQuota();
		}
	}

	public boolean remove(String tid)
	{
		log.info("Removing tenant: " + tid);
		return tenants.remove(tid) != null;
	}

	public boolean allocate(String tid, ResourceSpec spec) throws AllocationFailureException
	{
		final Tenant tenant = this.lookup(tid);
		synchronized (tenant) {
			return tenant.getQuota()
					.allocate(spec);
		}
	}

	public void free(String tid, ResourceSpec spec) throws AllocationFailureException
	{
		final Tenant tenant = this.lookup(tid);
		synchronized (tenant) {
			tenant.getQuota()
					.free(spec);
		}
	}


	// ========== Internal Stuff ====================

	@PostConstruct
	private void initialize()
	{
		this.restore();
	}

	@PreDestroy
	private void terminate()
	{
		this.save();
	}

	private void save()
	{
		final Path statepath = this.getStateDumpPath();

		log.info("Saving tenants to file...");
		try {
			final Collection<TenantConfig> entries = tenants.values().stream()
					.map(Tenant::getConfig)
					.collect(Collectors.toList());

			JsonUtils.store(statepath, entries, log);
		}
		catch (Exception error) {
			throw new IllegalStateException("Saving tenants failed!", error);
		}

		log.info(tenants.size() + " tenant(s) saved to: " + statepath.toString());
	}

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

	private Tenant lookup(String tid) throws AllocationFailureException
	{
		final Tenant tenant = tenants.get(tid);
		if (tenant == null)
			throw new AllocationFailureException("Unknown tenant ID: " + tid);

		return tenant;
	}
}
