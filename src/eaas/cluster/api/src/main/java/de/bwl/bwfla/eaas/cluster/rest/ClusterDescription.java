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

package de.bwl.bwfla.eaas.cluster.rest;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterDescription
{
	private final String name;
	private Collection<ResourceProviderDescription> providers = null;


	public ClusterDescription(String name)
	{
		this.name = name;
	}

	public ClusterDescription setResourceProviders(Collection<ResourceProviderDescription> providers)
	{
		this.providers = providers;
		return this;
	}

	@JsonProperty("name")
	public String getName()
	{
		return name;
	}

	@JsonProperty("num_resource_providers")
	public int getNumResourceProviders()
	{
		return (providers != null) ? providers.size() : 0;
	}

	@JsonProperty("resource_providers")
	public Collection<ResourceProviderDescription> getResourceProviders()
	{
		return providers;
	}
}
