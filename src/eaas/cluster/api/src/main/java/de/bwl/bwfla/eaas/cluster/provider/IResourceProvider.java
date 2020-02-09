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

package de.bwl.bwfla.eaas.cluster.provider;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import de.bwl.bwfla.eaas.cluster.ResourceHandle;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.metadata.LabelSelector;
import de.bwl.bwfla.eaas.cluster.metadata.LabelIndex;


public interface IResourceProvider extends IDumpable
{
	public CompletableFuture<ResourceHandle> allocate(UUID allocationId, ResourceSpec spec, boolean scaleup, long timeout, TimeUnit unit);
	public CompletableFuture<ResourceSpec> release(ResourceHandle handle);
	
	/** Initiates a proper shutdown of this provider. */
	public CompletableFuture<Boolean> shutdown();
	
	/** Terminates this provider immediately. */
	public boolean terminate();
	
	/** Returns true, when the specified label selector matches, else false. */
	public boolean apply(LabelSelector selector);
	
	/** Returns true, when all specified label selectors match, else false. */
	public boolean apply(Collection<LabelSelector> selectors);
	
	public LabelIndex getLabelIndex();
	public String getName();
}
