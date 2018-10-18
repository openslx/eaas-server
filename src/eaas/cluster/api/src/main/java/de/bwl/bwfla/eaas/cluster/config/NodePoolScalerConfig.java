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


public abstract class NodePoolScalerConfig extends BaseConfig
{
	@Config("pool_scaling_interval")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long poolScalingInterval = -1L;

	@Config("scaledown.node_warmup_period")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long nodeWarmUpPeriod = -1L;

	@Config("scaledown.node_cooldown_period")
	@WithPropertyConverter(DurationPropertyConverter.class)
	private long nodeCoolDownPeriod = -1L;


	/* ========== Getters and Setters ========== */

	public long getPoolScalingInterval()
	{
		return poolScalingInterval;
	}

	public void setPoolScalingInterval(long interval)
	{
		ConfigHelpers.check(interval, 0L, Long.MAX_VALUE, "Pool scaling interval is invalid!");
		this.poolScalingInterval = interval;
	}

	public void setPoolScalingInterval(long interval, TimeUnit unit)
	{
		this.setPoolScalingInterval(unit.toMillis(interval));
	}

	public long getNodeWarmUpPeriod()
	{
		return nodeWarmUpPeriod;
	}

	public void setNodeWarmUpPeriod(long period)
	{
		ConfigHelpers.check(period, 0L, Long.MAX_VALUE, "Node warm-up period is invalid!");
		this.nodeWarmUpPeriod = period;
	}

	public void setNodeWarmUpPeriod(long period, TimeUnit unit)
	{
		this.setNodeWarmUpPeriod(unit.toMillis(period));
	}

	public long getNodeCoolDownPeriod()
	{
		return nodeCoolDownPeriod;
	}

	public void setNodeCoolDownPeriod(long period)
	{
		ConfigHelpers.check(period, 0L, Long.MAX_VALUE, "Node cool-down period is invalid!");
		this.nodeCoolDownPeriod = period;
	}

	public void setNodeCoolDownPeriod(long period, TimeUnit unit)
	{
		this.setNodeCoolDownPeriod(unit.toMillis(period));
	}


	/* ========== Initialization ========== */

	@Override
	public void validate() throws ConfigException
	{
		// Re-check the arguments...
		this.setPoolScalingInterval(poolScalingInterval);
		this.setNodeWarmUpPeriod(nodeWarmUpPeriod);
		this.setNodeCoolDownPeriod(nodeCoolDownPeriod);
	}
	
	protected JsonObject dump()
	{
		final JsonObjectBuilder scaleDownJson = Json.createObjectBuilder()
				.add("node_warmup_period", DumpHelpers.toDurationString(nodeWarmUpPeriod))
				.add("node_cooldown_period", DumpHelpers.toDurationString(nodeCoolDownPeriod));

		return Json.createObjectBuilder()
				.add("pool_scaling_interval", DumpHelpers.toDurationString(poolScalingInterval))
				.add("scaledown", scaleDownJson.build())
				.build();
	}
	
	protected static class DumpFields
	{
		public static final String MIN_POOLSIZE  = "min_poolsize";
		public static final String MAX_POOLSIZE  = "max_poolsize";
		public static final String SCALEUP       = "scaleup";
		public static final String SCALEDOWN     = "scaledown";
		
		public static final String MAX_POOLSIZE_ADJUSTMENT  = "max_poolsize_adjustment";
	}
}
