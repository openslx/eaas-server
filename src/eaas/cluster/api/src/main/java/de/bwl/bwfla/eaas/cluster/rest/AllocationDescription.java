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
import de.bwl.bwfla.eaas.cluster.ResourceSpec;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllocationDescription
{
	private final String id;
	private ResourceSpec spec = null;

	public AllocationDescription(String id)
	{
		this.id = id;
	}

	public AllocationDescription setResourceSpec(ResourceSpec spec)
	{
		this.spec = spec;
		return this;
	}

	@JsonProperty("id")
	public String getId()
	{
		return id;
	}

	@JsonProperty("spec")
	public ResourceSpec getResourceSpec()
	{
		return spec;
	}
}
