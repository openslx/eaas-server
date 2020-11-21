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

package de.bwl.bwfla.eaas.acl;

import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Stream;


public abstract class AbstractLicenseController extends AbstractAccessController
{
	private final Map<String, SeatCounter> seats = new ConcurrentHashMap<>();
	private final Map<UUID, List<String>> allocations = new ConcurrentHashMap<>();

	protected AbstractLicenseController(int priority, Logger log)
	{
		super(priority, log);
	}

	protected void allocate(UUID session, Stream<ResourceHandle> resources) throws BWFLAException
	{
		final List<String> ids = allocations.computeIfAbsent(session, unused -> new ArrayList<>());
		try (resources) {
			final Iterator<ResourceHandle> iter = resources.iterator();
			while (iter.hasNext()) {
				final ResourceHandle resource = iter.next();
				this.allocate(resource.id(), resource.seats());
				ids.add(resource.id());
			}
		}
		catch (Exception error) {
			// Not all seats could be allocated, clean up!
			this.release(session);
			if (error instanceof BWFLAException)
				throw error;
			else throw new BWFLAException("Allocating license failed!", error);
		}
	}

	protected void release(UUID session)
	{
		final List<String> ids = allocations.remove(session);
		if (ids == null)
			return;  // No allocations made for this session!

		for (String id: ids)
			this.release(id);

		ids.clear();
	}

	private void allocate(String id, int maxseats) throws OutOfSeatsException
	{
		final SeatCounter counter = seats.computeIfAbsent(id, unused -> new SeatCounter());
		if (!counter.increment(maxseats))
			throw new OutOfSeatsException("Max. number of seats reached for resource '" + id + "'!");

		log.info("License for resource '" + id + "' allocated");
	}

	private void release(String id)
	{
		final BiFunction<String, SeatCounter, SeatCounter> functor = (key, counter) -> {
			// Remove counter, once the last seat has been released!
			return (counter.decrement() > 0) ? counter : null;
		};

		seats.computeIfPresent(id, functor);
		log.info("License for resource '" + id + "' released");
	}


	protected static class ResourceHandle
	{
		private final String id;
		private final int seats;

		public ResourceHandle(String id, int seats)
		{
			this.id = id;
			this.seats = seats;
		}

		public String id()
		{
			return id;
		}

		public int seats()
		{
			return seats;
		}
	}
}

class SeatCounter
{
	private final AtomicInteger counter;

	public SeatCounter()
	{
		this.counter = new AtomicInteger(0);
	}

	public boolean increment(int maxnum)
	{
		int newnum = counter.incrementAndGet();
		if (newnum > maxnum) {
			// Max number of seats reached!
			counter.decrementAndGet();
			return false;
		}

		return true;
	}

	public int decrement()
	{
		return counter.decrementAndGet();
	}
}
