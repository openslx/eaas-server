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


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceProviderDescription
{
	private final String name;
	private final String type;

	private int numRequestsTotal = 0;
	private int numRequestsDeferred = 0;
	private int numRequestsExpired = 0;
	private int numRequestsFailed = 0;

	private NodePoolDescription nodepool = null;


	public ResourceProviderDescription(String name, String type)
	{
		this.name = name;
		this.type = type;
	}

	public ResourceProviderDescription setNumRequestsTotal(int num)
	{
		this.numRequestsTotal = num;
		return this;
	}

	public ResourceProviderDescription setNumRequestsDeferred(int num)
	{
		this.numRequestsDeferred = num;
		return this;
	}

	public ResourceProviderDescription setNumRequestsExpired(int num)
	{
		this.numRequestsExpired = num;
		return this;
	}

	public ResourceProviderDescription setNumRequestsFailed(int num)
	{
		this.numRequestsFailed = num;
		return this;
	}

	public ResourceProviderDescription setNodePool(NodePoolDescription nodepool)
	{
		this.nodepool = nodepool;
		return this;
	}

	@JsonProperty("name")
	public String getName()
	{
		return name;
	}

	@JsonProperty("type")
	public String getType()
	{
		return type;
	}

	@JsonProperty("num_requests_total")
	public int getNumRequestsTotal()
	{
		return numRequestsTotal;
	}

	@JsonProperty("num_requests_deferred")
	public int getNumRequestsDeferred()
	{
		return numRequestsDeferred;
	}

	@JsonProperty("num_requests_expired")
	public int getNumRequestsExpired()
	{
		return numRequestsExpired;
	}

	@JsonProperty("num_requests_failed")
	public int getNumRequestsFailed()
	{
		return numRequestsFailed;
	}

	@JsonProperty("node_pool")
	public NodePoolDescription getNodePool()
	{
		return nodepool;
	}
}
