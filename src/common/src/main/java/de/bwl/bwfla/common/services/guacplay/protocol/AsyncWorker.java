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

package de.bwl.bwfla.common.services.guacplay.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.services.guacplay.util.ConditionVariable;
import de.bwl.bwfla.common.services.guacplay.util.PaddedAtomicInteger;


/** A worker for asynchronous processing. */
public abstract class AsyncWorker implements Runnable
{
	/** Logger instance. */
	protected final Logger log = LoggerFactory.getLogger(AsyncWorker.class);
	
	/** Processor's state */
	private final PaddedAtomicInteger state;
	
	/** Wakeup condition. */
	protected final ConditionVariable condition;
	
	
	/* ========== Internal Constants ========== */
	
	private static final long TIMEOUT_ON_TERMINATING = 100L;  // in ms
	
	/* Possible worker's states */
	private static final int STATE_IDLE         = 0;
	private static final int STATE_RUNNING      = 1;
	private static final int STATE_TERMINATING  = 2;
	
	
	/** Constructor */
	public AsyncWorker()
	{
		this.state = new PaddedAtomicInteger(STATE_IDLE);
		this.condition = new ConditionVariable();
	}
	
	/** Starts this worker in a background thread. */
	public void start()
	{
		(new Thread(this, this.getName())).start();
	}

	/**
	 * Request the termination of this worker.
	 * @param block If true, then block until this worker is terminated.
	 *              If false, then return immediately.
	 */
	public void terminate(boolean block)
	{
		// Signal atomically, that we want to exit
		state.compareAndSet(STATE_RUNNING, STATE_TERMINATING);
		condition.signalAll();
		
		if (block) {
			// Wait, until this worker terminates
			while (state.get() == STATE_TERMINATING)
				condition.await(TIMEOUT_ON_TERMINATING);
		}
	}
	
	/** Returns true, when the worker is in running-state, else false. */
	public final boolean isRunning()
	{
		return (state.get() == STATE_RUNNING);
	}
	
	/** Wakeup the worker, when waiting on the internal condition. */
	public void wakeup()
	{
		condition.signal();
	}
	
	
	/* ========== Runnable Implementation ========== */
	
	@Override
	public final void run()
	{
		// Ensure valid state
		if (state.get() != STATE_IDLE) {
			String errmsg = "Attempt to start an already running processor " + this.getName() + "!";
			throw new IllegalStateException(errmsg);
		}

		// Signal, that the processing begins
		state.set(STATE_RUNNING);

		final Thread thread = Thread.currentThread();
		log.info("{} started in thread {}.", this.getName(), thread.getId());

		try {
			do {
				// One processing step
				this.execute();
				
				// Handle possible interrupt
				if (thread.isInterrupted()) {
					log.warn("An interrupt for thread {} was requested, prepare to exit.", thread.getId());
					break;
				}
	
				// Repeat, until termination is requested!
			} while (state.get() == STATE_RUNNING);

			this.finish();  // Execute actions, before finishing
		}
		catch (Exception exception) {
			// Something gone wrong, rethrow!
			throw new RuntimeException(exception);
		}
		finally {
			// Signal to waiting threads, that we are done
			state.set(STATE_IDLE);
			condition.signalAll();
	
			log.info("{} stopped in thread {}.", this.getName(), thread.getId());
		}
	}
	
	/** Execute one processing step. */
	protected abstract void execute() throws Exception;
	
	/** This method will be called before termination. */
	protected abstract void finish() throws Exception;
	
	/** Returns the worker's name. */
	protected abstract String getName();
}
