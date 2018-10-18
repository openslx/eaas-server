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

package de.bwl.bwfla.common.services.guacplay.events;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;


/**
 * A special event, representing the begin of a visual-sync phase.
 * This event is fired, when a mouse button was pressed.
 */
public class VisualSyncBeginEvent extends GuacEvent
{
	// Member fields
	private Instruction instruction;
	private long timestamp;
	
	
	/** Constructor */
	public VisualSyncBeginEvent(Object source)
	{
		this(source, -1, null);
	}

	/**
	 * Constructor
	 * @param source The object, that sent this event.
	 * @param timestamp The timestamp, when the mouse click occured.
	 * @param instr The mouse-instruction that caused this event.
	 */
	public VisualSyncBeginEvent(Object source, long timestamp, Instruction instr)
	{
		super(EventType.VSYNC_BEGIN, source);
		this.instruction = instr;
		this.timestamp = timestamp;
	}

	/** Set the timestamp, when the mouse click occured. */
	public void setTimestamp(long timestamp)
	{
		super.processed = false;
		this.timestamp = timestamp;
	}

	/** Set the mouse-instruction, that caused the sync. */
	public void setIntruction(Instruction instr)
	{
		this.instruction = instr;
	}
	
	/** Returns the timestamp, when to start the sync-phase. */
	public long getTimestamp()
	{
		return timestamp;
	}

	/** Returns the mouse-instruction. */
	public Instruction getInstruction()
	{
		return instruction;
	}
}
