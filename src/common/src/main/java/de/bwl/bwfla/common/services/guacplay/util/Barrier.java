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
 * A thread synchronization primitive, that allows a predefined number
 * of threads to wait for each other, until they all reach the barrier.
 */
public final class Barrier extends Object
{
	private int numThreads;
	private int counter;
	private boolean flag;
	private Runnable runnable;
	

	/** Constructor */
	public Barrier(int numThreads)
	{
		this(numThreads, null);
	}
	
	/** Constructor */
	public Barrier(int numThreads, Runnable runnable)
	{
		this.numThreads = numThreads;
		this.counter = numThreads;
		this.flag = true;
		this.runnable = runnable;
	}

	/** Set a new {@link Runnable} to run, when all threads reach the barrier. */
	public void setRunnable(Runnable runnable)
	{
		this.runnable = runnable;
	}
	
	/** Returns true when some threads are waiting on this barrier, else false. */
	public synchronized boolean hasWaitingThreads()
	{
		return (counter != numThreads);
	}
	
	/**
	 * Wait until all threads have invoked this method
	 * and then run the runnable, if one was registered.
	 */
	public synchronized boolean await()
	{
		// Last thread arrived?
		if (--counter == 0) {
			// Yes, unblock all
			this.release(true);
		}
		else {
			// No, then block and wait
			final boolean oldflag = flag;
			try {
				while (flag == oldflag)
					this.wait();
			}
			catch (InterruptedException e) {
				// Restore the interrupted state
				Thread.currentThread().interrupt();
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Decrement the number of threads participating in the synchronization.
	 * @param execute If true, then execute the registered {@link Runnable}, else don't.
	 */
	public synchronized void leave(boolean execute)
	{
		--numThreads;
		
		// Last thread?
		if (--counter == 0) {
			// Yes, unblock all
			this.release(execute && (numThreads > 0));
		}
	}
	
	/** Release all waiting threads. */
	private final void release(boolean execute)
	{
		// Execute the runnable first
		if (execute && (runnable != null))
			runnable.run();
				
		// Prepare the barrier for reuse
		counter = numThreads;
		flag = !flag;
		
		// Unblock all waiting threads
		this.notifyAll();
	}
}
