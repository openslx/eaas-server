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

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;
import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpHelpers;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;

// package-private

class AllocationRequestQueue implements IDumpable
{
	private final PriorityQueue<AllocationRequest> requests;
	private final TreeSet<AllocationRequest> resources;
	private final MutableResourceSpec specsum;
	
	public AllocationRequestQueue()
	{
		this.requests = new PriorityQueue<AllocationRequest>(DEADLINE_COMPARATOR);
		this.resources = new TreeSet<AllocationRequest>(RESOURCE_COMPARATOR);
		this.specsum = new MutableResourceSpec();
	}
	
	public ResourceSpec getResourceSum()
	{
		return specsum;
	}
	
	public void add(AllocationRequest request)
	{
		specsum.add(request.getResourceSpec());
		requests.add(request);
		resources.add(request);
	}
	
	/**
	 * Returns a view of this queue, containing requests below the bound.
	 * @param bound The resource spec to be used as a bound
	 * @return Returns a view backed by this queue
	 */
	public AllocationRequestQueue.View filter(ResourceSpec bound)
	{
		return new View(bound);
	}
	
	/** Retrieves, but does not remove, a request with the smallest deadline. */
	public AllocationRequest peek()
	{
		return requests.peek();
	}
	
	/** Retrieves and removes a request with the smallest deadline. */
	public AllocationRequest poll()
	{
		AllocationRequest request = requests.poll();
		if (request != null) {
			specsum.sub(request.getResourceSpec());
			resources.remove(request);
		}
		
		return request;
	}
	
	public boolean isEmpty()
	{
		return requests.isEmpty();
	}
	
	public int size()
	{
		return requests.size();
	}
	
	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.RESOURCES_SUM, () -> {
				json.write(DumpFields.RESOURCES_SUM, DumpHelpers.toJsonObject(specsum));
			});
			
			dumper.add("num_" + DumpFields.ENTRIES, () -> json.write("num_" + DumpFields.ENTRIES, this.size()));
			dumper.add(DumpFields.ENTRIES, () -> {
				json.writeStartArray(DumpFields.ENTRIES);
				for (AllocationRequest request : requests) {
					json.writeStartObject();
					json.write("allocation_id", request.getAllocationId().toString());
					final long deadline = request.getDeadline() - ResourceProvider.getCurrentTime();
					json.write("deadline",  (deadline < 0) ? "expired" : DumpHelpers.toDurationString(deadline));
					json.write("spec", DumpHelpers.toJsonObject(request.getResourceSpec()));
					json.writeEnd();
				}
				
				json.writeEnd();
			});
			
			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String ENTRIES        = "entries";
		private static final String RESOURCES_SUM  = "resources_sum";
	}
	
	
	/** A class representing a subset of requests of an {@link AllocationRequestQueue} */
	public final class View
	{
		private final TreeSet<AllocationRequest> deadlines;
		private SortedSet<AllocationRequest> remaining;
		
		private View(ResourceSpec bound)
		{
			this.deadlines = new TreeSet<AllocationRequest>(DEADLINE_COMPARATOR);
			this.remaining = resources.headSet(this.newBoundKey(bound));
			
			// Sort the bounded requests by deadline
			deadlines.addAll(remaining);
		}
		
		/**
		 * Updates this view with a new bound.
		 * @param newbound The resource spec to be used as a new bound
		 */
		public void update(ResourceSpec newbound)
		{
			if (remaining.isEmpty())
				return;  // Nothing to do!
			
			final AllocationRequest curkey = this.newBoundKey(newbound);
			if (RESOURCE_COMPARATOR.compare(curkey, remaining.last()) >= 0)
				return;  // Remaining requests are below the new bound!
			
			// There must be some requests above the bound!
			// Remove them from the deadlines queue...
			deadlines.removeAll(remaining.tailSet(curkey));
		}
		
		/** Retrieves, but does not remove, a request with the smallest deadline. */
		public AllocationRequest peek()
		{
			return deadlines.first();
		}
		
		/** Retrieves and removes a request with the smallest deadline. */
		public AllocationRequest poll()
		{
			AllocationRequest request = deadlines.pollFirst();
			if (request != null)
				this.remove(request);
			
			return request;
		}
		
		/** Returns an iterator over view's requests sorted by deadline. */
		public Iterator<AllocationRequest> iterator()
		{
			return new ViewIterator(this);
		}
		
		public boolean isEmpty()
		{
			return deadlines.isEmpty();
		}
		
		public int size()
		{
			return deadlines.size();
		}
		
		private void remove(AllocationRequest request)
		{
			final AllocationRequestQueue outer = AllocationRequestQueue.this;
			
			// Remove request from the original queue
			outer.requests.remove(request);
			outer.resources.remove(request);
			outer.specsum.sub(request.getResourceSpec());
			
			// Since remaining set is backed by the
			// outer queue, nothing more to do!
		}
		
		private AllocationRequest newBoundKey(ResourceSpec bound)
		{
			// The returned object must be immutable, since TreeSet.headSet() and
			// TreeSet.tailSet() save the reference to that object internally!
			return new AllocationRequest(null, DUMMY_UUID, new ResourceSpec(bound), Long.MAX_VALUE);
		}
	}
	
	
	/* =============== Internal Helpers =============== */
	
	private static final UUID DUMMY_UUID = new UUID(0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL);
	
	/** Comparator for sorting by request's deadline */
	private static final Comparator<AllocationRequest> DEADLINE_COMPARATOR = (r1, r2) -> {
		final int result = Long.compare(r1.getDeadline(), r2.getDeadline());
		if (result != 0)
			return result;
		
		final UUID aid1 = r1.getAllocationId();
		return aid1.compareTo(r2.getAllocationId());
	};
	
	/** Comparator for sorting by request's resources */
	private static final Comparator<AllocationRequest> RESOURCE_COMPARATOR = (r1, r2) -> {
		final int result = ResourceSpec.compare(r1.getResourceSpec(), r2.getResourceSpec());
		if (result != 0)
			return result;
		
		return DEADLINE_COMPARATOR.compare(r1, r2);
	};
	
	private static final class ViewIterator implements Iterator<AllocationRequest>
	{
		private final AllocationRequestQueue.View view;
		private final Iterator<AllocationRequest> iterator;
		private AllocationRequest request;
		
		public ViewIterator(AllocationRequestQueue.View view)
		{
			this.view = view;
			this.iterator = view.deadlines.iterator();
			this.request = null;
		}
		
		@Override
		public boolean hasNext()
		{
			return iterator.hasNext();
		}
		
		@Override
		public AllocationRequest next()
		{
			request = iterator.next();
			return request;
		}
		
		@Override
		public void remove()
		{
			iterator.remove();
			if (request != null)
				view.remove(request);
		}
	}
}
