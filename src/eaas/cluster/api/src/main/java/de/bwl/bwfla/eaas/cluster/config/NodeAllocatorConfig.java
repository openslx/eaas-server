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

import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.WithPropertyConverter;

import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.config.util.DurationPropertyConverter;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;


public abstract class NodeAllocatorConfig extends BaseConfig
{
	@Config("subdomain_prefix")
	private String subdomainPrefix = "ec-";

	@Config("healthcheck.url_template")
	private String healthCheckUrl = null;
	
	@Config("healthcheck.connect_timeout")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long healthCheckConnectTimeout = -1L;
	
	@Config("healthcheck.read_timeout")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long healthCheckReadTimeout = -1L;
	
	@Config("healthcheck.failure_timeout")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long healthCheckFailureTimeout = -1L;
	
	@Config("healthcheck.interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long healthCheckInterval = -1L;
	
	@Config("healthcheck.num_parallel_requests")
	private int numParallelHealthChecks = -1;
	
	
	/* ========== Getters and Setters ========== */

	public String getSubDomainPrefix()
	{
		return subdomainPrefix;
	}

	public void setSubDomainPrefix(String prefix)
	{
		ConfigHelpers.check(prefix, "Subdomain prefix is invalid!");

		this.subdomainPrefix = prefix;
	}

	public String getHealthCheckUrl()
	{
		return healthCheckUrl;
	}
	
	public void setHealthCheckUrl(String url)
	{
		ConfigHelpers.check(url, "URL for heathchecks is invalid!");
		
		this.healthCheckUrl = url;
	}
	
	public long getHealthCheckConnectTimeout()
	{
		return healthCheckConnectTimeout;
	}
	
	public void setHealthCheckConnectTimeout(long timeout)
	{
		ConfigHelpers.check(timeout, 0L, Long.MAX_VALUE, "Timeout for healthcheck connect is invalid!");
		
		this.healthCheckConnectTimeout = timeout;
	}
	
	public void setHealthCheckConnectTimeout(long timeout, TimeUnit unit)
	{
		this.setHealthCheckConnectTimeout(unit.toMillis(timeout));
	}
	
	public long getHealthCheckReadTimeout()
	{
		return healthCheckReadTimeout;
	}
	
	public void setHealthCheckReadTimeout(long timeout)
	{
		ConfigHelpers.check(timeout, 0L, Long.MAX_VALUE, "Timeout for healthcheck read is invalid!");
		
		this.healthCheckReadTimeout = timeout;
	}
	
	public void setHealthCheckReadTimeout(long timeout, TimeUnit unit)
	{
		this.setHealthCheckReadTimeout(unit.toMillis(timeout));
	}
	
	public long getHealthCheckFailureTimeout()
	{
		return healthCheckFailureTimeout;
	}
	
	public void setHealthCheckFailureTimeout(long timeout)
	{
		ConfigHelpers.check(timeout, 0L, Long.MAX_VALUE, "Timeout for healthcheck failure is invalid!");
		
		this.healthCheckFailureTimeout = timeout;
	}
	
	public void setHealthCheckFailureTimeout(long timeout, TimeUnit unit)
	{
		this.setHealthCheckFailureTimeout(unit.toMillis(timeout));
	}
	
	public long getHealthCheckInterval()
	{
		return healthCheckInterval;
	}
	
	public void setHealthCheckInterval(long interval)
	{
		ConfigHelpers.check(interval, 0L, Long.MAX_VALUE, "Interval for healthchecks is invalid!");
		
		this.healthCheckInterval = interval;
	}
	
	public void setHealthCheckInterval(long interval, TimeUnit unit)
	{
		this.setHealthCheckInterval(unit.toMillis(interval));
	}
	
	public int getNumParallelHealthChecks()
	{
		return numParallelHealthChecks;
	}
	
	public void setNumParallelHealthChecks(int number)
	{
		ConfigHelpers.check(number, 1, 512, "Number of parallel healthchecks is invalid!");
		
		this.numParallelHealthChecks = number;
	}
	
	public abstract boolean hasHomogeneousNodes();
	
	
	/* ========== Initialization ========== */
	
	@Override
	public void validate() throws ConfigException
	{
		// Re-check the arguments...
		this.setSubDomainPrefix(subdomainPrefix);
		this.setHealthCheckUrl(healthCheckUrl);
		this.setHealthCheckConnectTimeout(healthCheckConnectTimeout);
		this.setHealthCheckReadTimeout(healthCheckReadTimeout);
		this.setHealthCheckFailureTimeout(healthCheckFailureTimeout);
		this.setHealthCheckInterval(healthCheckInterval);
		this.setNumParallelHealthChecks(numParallelHealthChecks);
	}
	
	protected JsonObject dump()
	{
		final JsonObjectBuilder json = Json.createObjectBuilder()
				.add("url_template", healthCheckUrl)
				.add("connect_timeout", DumpHelpers.toDurationString(healthCheckConnectTimeout))
				.add("read_timeout", DumpHelpers.toDurationString(healthCheckReadTimeout))
				.add("failure_timeout", DumpHelpers.toDurationString(healthCheckFailureTimeout))
				.add("interval", DumpHelpers.toDurationString(healthCheckInterval))
				.add("num_parallel_requests", numParallelHealthChecks);

		return Json.createObjectBuilder()
				.add("subdomain_prefix", subdomainPrefix)
				.add("healthcheck", json.build())
				.build();
	}
}
