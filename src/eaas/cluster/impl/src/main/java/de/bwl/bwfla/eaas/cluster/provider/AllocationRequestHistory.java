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

import java.util.ArrayDeque;
import java.util.Deque;

import de.bwl.bwfla.eaas.cluster.MutableResourceSpec;
import de.bwl.bwfla.eaas.cluster.ResourceSpec;

// package-private

class AllocationRequestHistory
{
	private final Deque<AllocationRequestHistory.Entry> requests;
	private final MutableResourceSpec curSpecSum;
	private final int maxNumRequests;
	private final long maxRequestAge;

	public AllocationRequestHistory(int maxNumRequests, long maxRequestAge)
	{
		this.requests = new ArrayDeque<AllocationRequestHistory.Entry>(maxNumRequests);
		this.curSpecSum = new MutableResourceSpec();
		this.maxNumRequests = maxNumRequests;
		this.maxRequestAge = maxRequestAge;
	}

	public void add(ResourceSpec spec)
	{
		// Request history tracking disabled?
		if (maxNumRequests <= 0)
			return;

		// Purge some older requests...
		while (requests.size() >= maxNumRequests) {
			AllocationRequestHistory.Entry entry = requests.poll();
			curSpecSum.sub(entry.resources());
		}

		long timestamp = ResourceProvider.getCurrentTime() + maxRequestAge;
		requests.add(new Entry(spec, timestamp));
		curSpecSum.add(spec);
	}

	public void update()
	{
		final long curtime = ResourceProvider.getCurrentTime();
		AllocationRequestHistory.Entry entry = null;

		// Remove all old requests...
		while ((entry = requests.peek()) != null) {
			if (entry.timestamp() > curtime)
				break;

			curSpecSum.sub(entry.resources());
			requests.poll();
		}
	}

	public void reset()
	{
		curSpecSum.reset();
		requests.clear();
	}

	public int size()
	{
		return requests.size();
	}

	public ResourceSpec getResourceSum()
	{
		return curSpecSum;
	}


	private static class Entry
	{
		private final long timestamp;
		private final ResourceSpec spec;

		public Entry(ResourceSpec spec, long timestamp)
		{
			this.timestamp = timestamp;
			this.spec = spec;
		}

		public ResourceSpec resources()
		{
			return spec;
		}

		public long timestamp()
		{
			return timestamp;
		}
	}
}