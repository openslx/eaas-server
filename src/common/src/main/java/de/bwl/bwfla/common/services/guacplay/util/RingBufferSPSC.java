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

// The code is based on the ideas from the following blog post:
// http://psy-lob-saw.blogspot.de/2013/03/single-producerconsumer-lock-free-queue.html


/**
 * A single-producer single-sonsumer ringbuffer with preallocated size
 * and single-phase or two-phase insertion and deletion operations.
 * 
 * <p/><b>NOTE:</b> Mixed use of single-phase and two-phase operations is not supported!
 */
public final class RingBufferSPSC<T>
{
	/** Preallocated entries */
	private final T[] entries;
	
	/** Current size of the buffer */
	private final PaddedAtomicInteger size;
	
	/** Position for reading */
	private final PaddedInteger ridx;
	
	/** Position for writing */
	private final PaddedInteger widx;
	
	
	/**
	 * Constructs a new ringbuffer.
	 * @param entries The preallocated buffer for storage of ringbuffer's entries.
	 */
	public RingBufferSPSC(T[] entries)
	{
		this.entries = entries;
		this.size = new PaddedAtomicInteger(0);
		this.ridx = new PaddedInteger(0);
		this.widx = new PaddedInteger(0);
	}
	
	/**
	 * Try to insert a new entry into the ringbuffer.
	 * @return true if the insertion was successful, else false.
	 * @see {@link #beginBlockingPutOp()}, {@link #finishPutOp()}
	 */
	public boolean put(T entry)
	{
		// Is the buffer full?
		if (size.get() == this.getCapacity())
			return false;  // Yes
		
		// No, add the new entry
		entries[widx.get()] = entry;
		this.finishPutOp();
		return true;
	}
	
	/**
	 * Prepare the ringbuffer for the insertion of a new entry.
	 * This operation must be commited to finish the insertion!
	 * @return The entry representing the element to insert.
	 *         If the buffer is full, then null is returned.
	 * 
	 * @see {@link #beginBlockingPutOp()}, {@link #finishPutOp()}
	 */
	public T beginPutOp()
	{
		// Is the buffer full?
		if (size.get() == this.getCapacity()) {
			// Yes, signal that the call would block.
			return null;
		}
		
		// No, return the current entry
		return entries[widx.get()];
	}
	
	/**
	 * Prepare the ringbuffer for the insertion of a new entry.
	 * This operation must be commited to finish the insertion!
	 * <p/> NOTE: When the ringbuffer is full, this call blocks.
	 * @return The entry representing the element to insert.
	 * 
	 * @see {@link #beginPutOp()}, {@link #finishPutOp()}
	 */
	public T beginBlockingPutOp()
	{
		T entry = null;
		
		// Retry, as long as the buffer is full 
		while ((entry = this.beginPutOp()) == null)
			Thread.yield();
		
		return entry;
	}
	
	/**
	 * Finishes a pending insertion operation.
	 * @return The current ringbuffer's size.
	 * @see {@link #beginPutOp()}, {@link #beginBlockingPutOp()}
	 */
	public int finishPutOp()
	{
		// Update the index for next put() call
		this.advance(widx);
		
		// Mark, that a new element is available
		return size.incrementAndGet();
	}
	
	/**
	 * Try to remove an entry from the ringbuffer.
	 * @return The deleted entry or null, when the buffer is empty.
	 * @see {@link #put(T)}
	 */
	public T take()
	{
		// Is the ringbuffer empty?
		if (size.get() == 0)
			return null;  // Yes
		
		// No, return the current entry
		final int index = ridx.get();
		final T entry = entries[index];
		this.finishTakeOp();
		return entry;
	}
	
	/**
	 * Prepare the ringbuffer for the deletion of an entry.
	 * This operation must be commited to finish the deletion!
	 * @return The deleted entry or null, when the buffer is empty.
	 * 
	 * @see {@link #beginBlockingTakeOp()}, {@link #finishTakeOp()}
	 */
	public T beginTakeOp()
	{
		// Is the ringbuffer empty?
		if (size.get() == 0) {
			// Yes, signal that the call would block.
			return null;
		}
		
		// No, return the current entry
		return entries[ridx.get()];
	}
	
	/**
	 * Prepare the ringbuffer for the deletion of an entry.
	 * This operation must be commited to finish the deletion!
	 * <p/> NOTE: When the ringbuffer is empty, this call blocks.
	 * @return The entry representing the element to delete.
	 * 
	 * @see {@link #beginTakeOp()}, {@link #finishTakeOp()}
	 */
	public T beginBlockingTakeOp()
	{
		T entry = null;
		
		// Retry, as long as the buffer is empty 
		while ((entry = this.beginTakeOp()) == null)
			Thread.yield();
		
		return entry;
	}
	
	/**
	 * Finishes a pending deletion operation.
	 * @return The current ringbuffer's size.
	 * @see {@link #beginTakeOp()}, {@link #beginBlockingTakeOp()}
	 */
	public int finishTakeOp()
	{
		// Update the index for next take() call
		this.advance(ridx);
		
		// Mark, that a new element was removed
		return size.decrementAndGet();
	}
	
	/** Returns true when the ringbuffer is empty, else false. */
	public boolean isEmpty()
	{
		return (size.get() == 0);
	}
	
	/** Returns the number of valid entries in the ringbuffer. */
	public int getSize()
	{
		return size.get();
	}
	
	
	/* =============== Internal Methods =============== */
	
	/** Returns the buffer's capacity. */
	private final int getCapacity()
	{
		return entries.length;
	}
	
	/** Update the index for next put() or take() call. */
	private final void advance(PaddedInteger index)
	{
		final int next = index.get() + 1;
				
		// If the new index would wrap around the buffer's capacity,
		// reset it to 0 using the following sign-extension trick.
		//
		// mask := (index - capacity) >> 31
		//
		// Case (index < capacity):
		//     index & mask = index & 0xFFFFFFFF = index
		//
		// Case (index >= capacity):
		//     index & mask = index & 0x0 = 0
		
		final int mask = (next - this.getCapacity()) >> 31;
		index.set(next & mask);
	}
}
