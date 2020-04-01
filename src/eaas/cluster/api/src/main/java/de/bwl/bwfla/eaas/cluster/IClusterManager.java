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

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.exception.AllocationFailureException;
import de.bwl.bwfla.eaas.cluster.exception.OutOfResourcesException;
import de.bwl.bwfla.eaas.cluster.metadata.LabelSelector;
import de.bwl.bwfla.eaas.cluster.provider.IResourceProvider;
import de.bwl.bwfla.eaas.cluster.rest.ClusterDescription;


public interface IClusterManager extends IDumpable, IDescribable<ClusterDescription>
{
    public ResourceHandle allocate(String tenant, UUID aid, ResourceSpec spec, Duration duration)
            throws TimeoutException, AllocationFailureException, OutOfResourcesException;
    
    public ResourceHandle allocate(String tenant, UUID aid, ResourceSpec spec, long timeout, TimeUnit unit)
			throws TimeoutException, AllocationFailureException, OutOfResourcesException;
	
    public ResourceHandle allocate(String tenant, LabelSelector selector, UUID aid, ResourceSpec spec, Duration duration)
			throws TimeoutException, AllocationFailureException, OutOfResourcesException;
    
	public ResourceHandle allocate(
			String tenant, LabelSelector selector, UUID aid, ResourceSpec spec, long timeout, TimeUnit unit)
			throws TimeoutException, AllocationFailureException, OutOfResourcesException;
	
	public ResourceHandle allocate(
			String tenant, Collection<LabelSelector> selectors, UUID aid, ResourceSpec spec, Duration duration)
			throws TimeoutException, AllocationFailureException, OutOfResourcesException;
	
	public ResourceHandle allocate(
			String tenant, Collection<LabelSelector> selectors, UUID aid, ResourceSpec spec, long timeout, TimeUnit unit)
			throws TimeoutException, AllocationFailureException, OutOfResourcesException;
			
	public void release(ResourceHandle handle);
	
	public CompletableFuture<Boolean> shutdown();
	
	public void setResourceProviderComparator(Comparator<IResourceProvider> comparator);
	public Comparator<IResourceProvider> getResourceProviderComparator();
	public String getName();
	public Collection<String> getProviderNames();
}
