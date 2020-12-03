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

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;


public class Tenant
{
	private final String name;
	private final Quota quota;

	public Tenant(TenantConfig config)
	{
		this.name = config.getName();
		this.quota = new Quota(config.getQuotaLimits());
	}

	public Quota getQuota()
	{
		return quota;
	}

	public TenantConfig getConfig()
	{
		final TenantConfig config = new TenantConfig();
		config.setName(name);
		config.setQuotaLimits(quota.getLimits());
		return config;
	}


	public static class Quota
	{
		private final MutableResourceSpec limits;
		private final MutableResourceSpec allocated;

		public Quota()
		{
			this(new MutableResourceSpec(Integer.MAX_VALUE, Integer.MAX_VALUE));
		}

		public Quota(ResourceSpec limits)
		{
			this.limits = new MutableResourceSpec(limits);
			this.allocated = new MutableResourceSpec();
		}

		public Quota(Quota other)
		{
			this.limits = new MutableResourceSpec(other.getLimits());
			this.allocated = new MutableResourceSpec(other.getAllocated());
		}

		public Quota setLimits(ResourceSpec limits)
		{
			this.limits.set(limits);
			return this;
		}

		@JsonProperty("limits")
		public ResourceSpec getLimits()
		{
			return limits;
		}

		@JsonProperty("allocated")
		public ResourceSpec getAllocated()
		{
			return allocated;
		}

		public boolean allocate(ResourceSpec spec)
		{
			allocated.add(spec);

			if (allocated.cpu() > limits.cpu() || allocated.memory() > limits.memory()) {
				// Quota reached, revert...
				allocated.sub(spec);
				return false;
			}

			return true;
		}

		public void free(ResourceSpec spec)
		{
			allocated.sub(spec);
		}
	}
}
