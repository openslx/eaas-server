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

public class NodeID implements Comparable<NodeID>
{
	private final String id;
	private String fqdn = null;
	private String protocol = "";
	
	public NodeID(String id)
	{
		if (id == null || id.isEmpty())
			throw new IllegalArgumentException("Node ID is invalid!");
		
		this.id = id;
	}
	
    public String getNodeHost() {
		if(fqdn != null)
			return fqdn;
        return protocol + id;
    }

    public String toString() {
        return id;
    }
	
	@Override
	public int compareTo(NodeID other)
	{
		return id.compareTo(other.id);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other) return true;
		if (other == null) return false;
		if (this.getClass() != other.getClass()) return false;

		return id.equals(((NodeID) other).id);
	}

	public void setFqdn(String fqdn) {
		this.fqdn = fqdn;
	}
	public void setProtocol(String proto) {
		this.protocol = proto;
	}
}
