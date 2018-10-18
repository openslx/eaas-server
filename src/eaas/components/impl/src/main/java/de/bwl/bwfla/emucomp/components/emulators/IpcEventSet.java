package de.bwl.bwfla.emucomp.components.emulators;

import de.bwl.bwfla.common.services.guacplay.util.StopWatch;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.EventID;


/** This class represents a set of predefined events. */
public class IpcEventSet
{
	// Member fields
	private final EventCounter[] events;

	private static final int EVENTS_CAPACITY = 5 + 1;
	
	/** Constructor */
	public IpcEventSet()
	{
		this.events = new EventCounter[EVENTS_CAPACITY];
		for (int i = 0; i < events.length; ++i)
			events[i] = new EventCounter(0);
	}
	
	/** Adds a new event to this set. */
	public void add(byte eventid)
	{
		this.check(eventid);
		
		EventCounter counter = events[eventid];
		synchronized (counter) {
			++counter.value;
			counter.notify();
		}
	}
	
	/** Retrieves a single event from this set, if available. */
	public boolean poll(byte eventid)
	{
		this.check(eventid);
		
		EventCounter counter = events[eventid];
		synchronized (counter) {
			if (counter.value > 0) {
				--counter.value;
				return true;
			}
		}
		
		return false;
	}
	
	/** Waits for an event to be added to this set. */
	public boolean await(byte eventid, long timeout) throws InterruptedException
	{
		this.check(eventid);
		
		EventCounter counter = events[eventid];
		synchronized (counter) {
			// According to the documentation, Object.wait() can spuriously exit without
			// timing out or being notified! Hence additional checks are needed here.
			StopWatch stopwatch = new StopWatch();
			while ((counter.value < 1) && (timeout > 0)) {
				stopwatch.start();
				counter.wait(timeout);
				final long elapsed = stopwatch.timems();
				if (elapsed >= timeout)
					break;
				
				timeout -= elapsed;
			}
			
			if (counter.value > 0) {
				--counter.value;
				return true;
			}
		}
		
		return false;
	}
	
	/** Waits for an event to be added to this set. */
	public void await(byte eventid) throws InterruptedException
	{
		this.await(eventid, Long.MAX_VALUE);
	}
	
	
	/* ==================== Internal Methods ==================== */
	
	private void check(byte eventid)
	{
		if (eventid < EventID.EMULATOR_CTLSOCK_READY || eventid > EventID.CLIENT_INACTIVE)
			throw new IllegalArgumentException("Passed EventID is invalid!");
	}
	
	
	private static final class EventCounter
	{
		public int value;
		
		public EventCounter(int value)
		{
			this.value = value;
		}
	}
}
