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
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;

import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.config.util.ConfigHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;


public class HeterogeneousNodePoolScalerConfig extends NodePoolScalerConfig
{
	private ResourceSpec minPoolSize = null;
	private ResourceSpec maxPoolSize = null;
	private ResourceSpec maxScaleUpAdjustment = null;
	private ResourceSpec maxScaleDownAdjustment = null;


	/* ========== Getters and Setters ========== */

	public ResourceSpec getMinPoolSize()
	{
		return minPoolSize;
	}

	public void setMinPoolSize(ResourceSpec minPoolSize)
	{
		ConfigHelpers.check(minPoolSize, "Min. poolsize (resource spec) is invalid!");
		this.minPoolSize = minPoolSize;
	}

	public ResourceSpec getMaxPoolSize()
	{
		return maxPoolSize;
	}

	public void setMaxPoolSize(ResourceSpec maxPoolSize)
	{
		ConfigHelpers.check(maxPoolSize, "Max. poolsize (resource spec) is invalid!");
		this.maxPoolSize = maxPoolSize;
	}

	public ResourceSpec getMaxPoolSizeScaleUpAdjustment()
	{
		return maxScaleUpAdjustment;
	}

	public void setMaxPoolSizeScaleUpAdjustment(ResourceSpec adjustment)
	{
		final String message = "Max. poolsize adjustment (resource spec) for scaling up is invalid!";
		ConfigHelpers.check(adjustment, message);
		this.maxScaleUpAdjustment = adjustment;
	}

	public ResourceSpec getMaxPoolSizeScaleDownAdjustment()
	{
		return maxScaleDownAdjustment;
	}

	public void setMaxPoolSizeScaleDownAdjustment(ResourceSpec adjustment)
	{
		final String message = "Max. poolsize adjustment (resource spec) for scaling down is invalid!";
		ConfigHelpers.check(adjustment, message);
		this.maxScaleDownAdjustment = adjustment;
	}


	/* ========== Initialization ========== */

	@Override
	public void load(Configuration config) throws ConfigException
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);

		// Configure min. poolsize for this instance
		{
			Configuration newconfig = ConfigHelpers.filter(config, "min_poolsize.");
			minPoolSize = ConfigHelpers.toResourceSpec(newconfig);
		}

		// Configure max. poolsize for this instance
		{
			Configuration newconfig = ConfigHelpers.filter(config, "max_poolsize.");
			maxPoolSize = ConfigHelpers.toResourceSpec(newconfig);
		}

		// Configure max. poolsize adjustment for scale-up
		{
			Configuration newconfig = ConfigHelpers.filter(config, "scaleup.max_poolsize_adjustment.");
			maxScaleUpAdjustment = ConfigHelpers.toResourceSpec(newconfig);
		}

		// Configure max. poolsize adjustment for scale-down
		{
			Configuration newconfig = ConfigHelpers.filter(config, "scaledown.max_poolsize_adjustment.");
			maxScaleDownAdjustment = ConfigHelpers.toResourceSpec(newconfig);
		}
	}

	@Override
	public void validate() throws ConfigException
	{
		this.valid = false;

		// Re-check the arguments...
		super.validate();
		this.setMinPoolSize(minPoolSize);
		this.setMaxPoolSize(maxPoolSize);
		if (ResourceSpec.compare(minPoolSize, maxPoolSize) > 0) {
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
			final JsonObjectBuilder builder = Json.createObjectBuilder();
			DumpHelpers.add(builder, DumpFields.MIN_POOLSIZE, minPoolSize);
			DumpHelpers.add(builder, DumpFields.MAX_POOLSIZE, maxPoolSize);
			builder.add(DumpFields.SCALEUP, Json.createObjectBuilder()
					.add(DumpFields.MAX_POOLSIZE_ADJUSTMENT, DumpHelpers.toJsonObject(maxScaleUpAdjustment)));
			builder.add(DumpFields.SCALEDOWN, Json.createObjectBuilder()
					.add(DumpFields.MAX_POOLSIZE_ADJUSTMENT, DumpHelpers.toJsonObject(maxScaleDownAdjustment)));
	
			final JsonObject object = DumpHelpers.merge(builder.build(), super.dump());
			object.forEach((key, value) -> {
				dumper.add(key, () -> json.write(key, value));
			});
	
			dumper.run();
		});
		
		trigger.run();
	}
}
