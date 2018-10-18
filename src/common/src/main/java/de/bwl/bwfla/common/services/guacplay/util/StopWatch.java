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

package de.bwl.bwfla.common.services.guacplay.util;

import java.util.concurrent.TimeUnit;


/** A simple stopwatch timer, for measuring time durations. */
public final class StopWatch
{
	/** Number of nanoseconds in one millisecond. */
	private static final long NANOSECONDS_IN_ONE_MILLISECOND
			= TimeUtils.convert(1L, TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS);
	
	private long stime;
	
	/** Constructor */
	public StopWatch()
	{
		this.start();
	}
	
	/** Start the timer. */
	public void start()
	{
		stime = System.nanoTime();
	}
	
	/**
	 * Returns the time duration in nano-seconds, since last start() call.
	 * @see {@link System nanoTime()}
	 */
	public long time()
	{
		return (System.nanoTime() - stime);
	}
	
	/** Returns the time duration in milli-seconds, since last start() call. */
	public long timems()
	{
		return this.time() / NANOSECONDS_IN_ONE_MILLISECOND;
	}
}
