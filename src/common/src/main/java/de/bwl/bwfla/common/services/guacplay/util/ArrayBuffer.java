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
 * A base class for wrapping plain arrays, with helper methods
 * for accessing the elements in the underlying array.
 */
public class ArrayBuffer
{
	protected int offset;
	protected int length;
	protected int position;
	protected int limit;
	
	
	/* =============== Public API =============== */
	
	/** Returns the offset of the wrapped array. */
	public final int offset()
	{
		return offset;
	}
	
	/** Returns the length of the wrapped array. */
	public final int length()
	{
		return length;
	}
	
	/** Returns the current position. */
	public final int position()
	{
		return position;
	}
	
	/** Set a new position. */
	public final void setPosition(int newpos)
	{
		if ((newpos < offset) || (newpos >= limit)) {
			String message = "Attempt to set an invalid position " + newpos + "! "
					+ "The currently valid range is [" + offset + ", " + (limit - 1) + "].";
			throw new IndexOutOfBoundsException(message);
		}
		
		this.position = newpos;
	}
	
	/** Returns the current limit value. */
	public final int limit()
	{
		return limit;
	}
	
	/** Returns the number of elements between the current position and limit. */
	public final int remaining()
	{
		return (limit - position);
	}
	
	/** Skip specified number of elements. */
	public final void skip(int n)
	{
		position += n;
	}
	
	/** Skip specified number of elements with bounds-checking. */
	public final void skipChecked(int n)
	{
		this.setPosition(position + n);
	}
	
	/** Reset the position to the array's offset. */
	public final void reset()
	{
		position = offset;
	}
	
	
	/* =============== Protected API =============== */
	
	/** Update the wrapper with new content. */
	protected void set(int offset, int length)
	{
		this.offset = offset;
		this.length = length;
		this.position = offset;
		this.limit = offset + length;
	}
	
	protected final void checkUnderflow(int pos)
	{
		if (pos >= limit)
			throw new IndexOutOfBoundsException("Array underflow occured.");
	}
}
