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


/** This class represents an event of a specific type. */
public class GuacEvent
{
	private final Object source;
	private final int type;
	protected boolean processed;
	
	
	/** Constructor */
	public GuacEvent(int type, Object source)
	{
		this.source = source;
		this.type = type;
		this.processed = false;
	}
	
	/** Returns the object, that produced this event. */
	public final Object getSource()
	{
		return source;
	}
	
	/** Returns the type of this event. */
	public final int getType()
	{
		return type;
	}
	
	/** Returns true, when this event was already processed, else false. */
	public final boolean isProcessed()
	{
		return processed;
	}
	
	/** Mark, that this event was processed and can be skipped. */
	public final void markAsProcessed()
	{
		processed = true;
	}
}
