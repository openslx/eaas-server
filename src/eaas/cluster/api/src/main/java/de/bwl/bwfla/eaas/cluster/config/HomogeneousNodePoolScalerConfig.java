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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;


public class HomogeneousNodePoolScalerConfig extends NodePoolScalerConfig
{
	@Config("min_poolsize")
	private int minPoolSize = -1;
	
	@Config("max_poolsize")
	private int maxPoolSize = -1;
	
	@Config("scaleup.max_poolsize_adjustment")
	private int maxScaleUpAdjustment = -1;
	
	@Config("scaledown.max_poolsize_adjustment")
	private int maxScaleDownAdjustment = -1;
	
	
	/* ========== Getters and Setters ========== */
	
	public int getMinPoolSize()
	{
		return minPoolSize;
	}

	public void setMinPoolSize(int minPoolSize)
	{
		ConfigHelpers.check(minPoolSize, 0, 1000, "Min. poolsize (number of nodes) is invalid!");
		this.minPoolSize = minPoolSize;
	}

	public int getMaxPoolSize()
	{
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize)
	{
		ConfigHelpers.check(maxPoolSize, 0, 2000, "Max. poolsize (number of nodes) is invalid!");
		this.maxPoolSize = maxPoolSize;
	}
	
	public int getMaxPoolSizeScaleUpAdjustment()
	{
		return maxScaleUpAdjustment;
	}

	public void setMaxPoolSizeScaleUpAdjustment(int adjustment)
	{
		final String message = "Max. poolsize adjustment (number of nodes) for scaling up is invalid!";
		ConfigHelpers.check(adjustment, 0, Integer.MAX_VALUE, message);
		this.maxScaleUpAdjustment = adjustment;
	}
	
	public int getMaxPoolSizeScaleDownAdjustment()
	{
		return maxScaleDownAdjustment;
	}

	public void setMaxPoolSizeScaleDownAdjustment(int adjustment)
	{
		final String message = "Max. poolsize adjustment (number of nodes) for scaling down is invalid!";
		ConfigHelpers.check(adjustment, 0, Integer.MAX_VALUE, message);
		this.maxScaleDownAdjustment = adjustment;
	}

	
	/* ========== Initialization ========== */

	@Override
	public void load(Configuration config) throws ConfigException
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);
	}
	
	@Override
	public void validate() throws ConfigException
	{
		this.valid = false;
		
		// Re-check the arguments...
		super.validate();
		this.setMinPoolSize(minPoolSize);
		this.setMaxPoolSize(maxPoolSize);
		if (minPoolSize > maxPoolSize) {
			final String message = "Pool scaler is misconfigured! " 
					+ "min_poolsize > max_poolsize";
			
			throw new ConfigException(message);
		}
		
		this.setMaxPoolSizeScaleUpAdjustment(maxScaleUpAdjustment);
		this.setMaxPoolSizeScaleDownAdjustment(maxScaleDownAdjustment);
		
		this.valid = true;
	}
	
	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			JsonObject object = Json.createObjectBuilder()
					.add(DumpFields.MIN_POOLSIZE, minPoolSize)
					.add(DumpFields.MAX_POOLSIZE, maxPoolSize)
					.add(DumpFields.SCALEUP, Json.createObjectBuilder()
							.add(DumpFields.MAX_POOLSIZE_ADJUSTMENT, maxScaleUpAdjustment))
					.add(DumpFields.SCALEDOWN, Json.createObjectBuilder()
							.add(DumpFields.MAX_POOLSIZE_ADJUSTMENT, maxScaleDownAdjustment))
					.build();
	
			object = DumpHelpers.merge(object, super.dump());
			object.forEach((key, value) -> {
				dumper.add(key, () -> json.write(key, value));
			});
	
			dumper.run();
		});
		
		trigger.run();
	}
}
