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


/**
 * A thread synchronization primitive, implemented as a wrapper around
 * the {@link Object#wait()} and {@link Object#notify()} methods.
 */
public final class ConditionVariable extends Object
{
	private boolean flag;
	
	
	/** Constructor */
	public ConditionVariable()
	{
		this.flag = false;
	}
	
	/**
	 * Block the current thread and wait, until some other thread
	 * invokes one of the signal methods of this class.
	 * @return true when the current thread was interrupted, else false.
	 * @see {@link #signal()}, {@link #signalAll()}
	 */
	public synchronized boolean await()
	{
		final boolean oldflag = flag;
		try {
			// Wait until notified
			while (flag == oldflag)
				this.wait();
		}
		catch (InterruptedException e) {
			// Restore the interrupted state
			Thread.currentThread().interrupt();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Block the current thread and wait, until some other thread invokes one
	 * of the signal methods or the specified amount of time has elapsed. <p/>
	 * <b>NOTE: </b>
	 *     A thread can also wake up without being notified, interrupted,
	 *     or timing out! See {@link Object#wait(long)} for more details.
	 * 
	 * @param timeout The maximum time to wait in milliseconds.
	 * @return true when the current thread was interrupted, else false.
	 * @see {@link #signal()}, {@link #signalAll()} 
	 */
	public synchronized boolean await(long timeout)
	{
		try {
			this.wait(timeout);
		}
		catch (InterruptedException e) {
			// Restore the interrupted state
			Thread.currentThread().interrupt();
			return true;
		}
		
		return false;
	}
	
	/** Wrapper for {@link Object#notify()}'s method. */
	public synchronized void signal()
	{
		flag = !flag;
		this.notify();
	}
	
	/** Wrapper for {@link Object#notifyAll()}'s method. */
	public synchronized void signalAll()
	{
		flag = !flag;
		this.notifyAll();
	}
}
