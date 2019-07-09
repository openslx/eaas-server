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


public class NodeID implements Comparable<NodeID>
{
	private final String ip;
	private String fqdn = null;
	private String protocol = "";
	
	public NodeID(String ip)
	{
		if (ip == null || ip.isEmpty())
			throw new IllegalArgumentException("Node IP is invalid!");
		
		this.ip = ip;
	}

	public NodeID setProtocol(String protocol)
	{
		this.protocol = protocol;
		return this;
	}

	public String getIpAddress()
	{
		return ip;
	}

	public String getDomainName()
	{
		return fqdn;
	}

	public NodeID setDomainName(String fqdn)
	{
		this.fqdn = fqdn;
		return this;
	}

    public String getNodeAddress()
	{
		return protocol + ((fqdn != null) ? fqdn : ip);
    }

    public String toString()
	{
		String nid = ip;
		if (fqdn != null)
			nid += " (" + fqdn + ")";

        return nid;
    }
	
	@Override
	public int compareTo(NodeID other)
	{
		return ip.compareTo(other.ip);
	}

	@Override
	public int hashCode()
	{
		return ip.hashCode();
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other) return true;
		if (other == null) return false;
		if (this.getClass() != other.getClass()) return false;

		return ip.equals(((NodeID) other).ip);
	}
}
