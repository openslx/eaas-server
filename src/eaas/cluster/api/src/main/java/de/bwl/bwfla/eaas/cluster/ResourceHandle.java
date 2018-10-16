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

package de.bwl.bwfla.eaas.cluster;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ResourceHandle implements Comparable<ResourceHandle>
{
	private static final char SEPARATOR = '+';
	
	private final String provider;
	private final NodeID nid;
	private final UUID aid;
	
	public ResourceHandle(String provider, NodeID nid, UUID aid)
	{
		if (provider == null || provider.isEmpty())
			throw new IllegalArgumentException("Provider name is invalid!");
		
		if (nid == null)
			throw new IllegalArgumentException("Node ID is invalid!");
		
		if (aid == null)
			throw new IllegalArgumentException("Allocation ID is invalid!");
		
		this.provider = provider;
		this.nid = nid;
		this.aid = aid;
	}
	
	public String getProviderName()
	{
		return provider;
	}
	
	public NodeID getNodeID()
	{
		return nid;
	}
	
	public UUID getAllocationID()
	{
		return aid;
	}
	
	public String toString()
	{
		return (provider + SEPARATOR + nid.toString() + SEPARATOR + aid.toString());
	}
	
	@Override
	public int compareTo(ResourceHandle other)
	{
		int result = provider.compareTo(other.provider);
		if (result != 0)
			return result;
		
		result = nid.compareTo(other.nid);
		if (result != 0)
			return result;
		
		return aid.compareTo(other.aid);
	}
	
	
	public static ResourceHandle fromString(String id)
	{
		final String expr = "([^" + SEPARATOR + "]+)";
		final String delim = "\\" + SEPARATOR;
		final Pattern pattern = Pattern.compile(expr + delim + expr + delim + expr);
		final Matcher matcher = pattern.matcher(id);
		if (!matcher.matches())
			throw new IllegalArgumentException("Malformed ID:  " + id);

		final String provider = matcher.group(1);
		final String nid = matcher.group(2);
		final String aid = matcher.group(3);
		return new ResourceHandle(provider, new NodeID(nid), UUID.fromString(aid));
	}
}
