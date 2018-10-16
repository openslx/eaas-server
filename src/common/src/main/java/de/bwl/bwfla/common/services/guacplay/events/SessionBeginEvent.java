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


/** An event, representing the begin of a session. */
public class SessionBeginEvent extends GuacEvent
{
	private long timestamp;
	
	
	/** Constructor */
	public SessionBeginEvent(Object source)
	{
		this(source, -1);
	}

	/**
	 * Constructor
	 * @param source The object, that sent this event.
	 * @param timestamp The timestamp, when the session started.
	 */
	public SessionBeginEvent(Object source, long timestamp)
	{
		super(EventType.SESSION_BEGIN, source);
		this.timestamp = timestamp;
	}

	/** Set the timestamp, when the session started. */
	public void setTimestamp(long timestamp)
	{
		super.processed = false;
		this.timestamp = timestamp;
	}
	
	/** Returns the timestamp of session's start. */
	public long getTimestamp()
	{
		return timestamp;
	}
}
