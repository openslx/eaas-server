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

import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;


/** A simple class representing a Guacamole's message. */
public final class Message
{
	private SourceType source;
	private long timestamp;
	private char[] data;
	private int offset;
	private int length;
	
	
	/** Constructor */
	public Message()
	{
		this.source = SourceType.UNKNOWN;
		this.timestamp = 0L;
		this.data = null;
		this.offset = 0;
		this.length = 0;
	}
	
	/** Constructor */
	public Message(SourceType source)
	{
		this.source = source;
		this.timestamp = 0L;
		this.data = null;
		this.offset = 0;
		this.length = 0;
	}
	
	/** Constructor */
	public Message(SourceType source, long timestamp, char[] data, int offset, int length)
	{
		this.source = source;
		this.timestamp = timestamp;
		this.data = data;
		this.offset = offset;
		this.length = length;
	}
	
	/** Reset this message. */
	public void reset()
	{
		this.set(0L, null, 0, 0);
	}
	
	/** Update the message's contents. */
	public void set(long timestamp, char[] data, int offset, int length)
	{
		this.timestamp = timestamp;
		this.data = data;
		this.offset = offset;
		this.length = length;
	}
	
	/** Update the message's contents. */
	public void set(SourceType source, long timestamp, char[] data, int offset, int length)
	{
		this.source = source;
		this.timestamp = timestamp;
		this.data = data;
		this.offset = offset;
		this.length = length;
	}

	/** Returns the message's source type. */
	public SourceType getSourceType()
	{
		return source;
	}
	
	/** Returns the message's timestamp. */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	/** Returns the underlying data array. */
	public char[] getDataArray()
	{
		return data;
	}
	
	/** Returns the data offset in the array. */
	public int getOffset()
	{
		return offset;
	}
	
	/** Returns the data length. */
	public int getLength()
	{
		return length;
	}
}
