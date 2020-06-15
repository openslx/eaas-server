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

package de.bwl.bwfla.eaas.cluster.provider.allocation;

import java.util.Collection;
import java.util.UUID;

import de.bwl.bwfla.eaas.cluster.IDescribable;
import de.bwl.bwfla.eaas.cluster.NodeID;
import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.provider.Node;
import de.bwl.bwfla.eaas.cluster.rest.NodeDescription;


public interface IResourceAllocator extends IDumpable, IDescribable<Collection<NodeDescription>>
{
	public boolean registerNode(Node node);
	public void unregisterNode(NodeID id);
	
	public ResourceSpec getFreeResources();
	public ResourceSpec getUsedResources();
	public int getNumAllocations();
	
	public ResourceHandle allocate(UUID allocationId, ResourceSpec spec);
	public ResourceSpec release(ResourceHandle id);
}
