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


/** A simple wrapper for plain-arrays. */
public class ArrayWrapper
{
	// Member fields
	protected int offset;
	protected int length;
	
	
	/** Constructor */
	protected ArrayWrapper()
	{
		this.offset = -1;
		this.length = -1;
	}
	
	/** Set the array's offset and length. */
	protected void set(int offset, int length)
	{
		this.offset = offset;
		this.length = length;
	}
	
	/** Returns the array's offset. */
	public final int offset()
	{
		return offset;
	}
	
	/** Returns the array's length. */
	public final int length()
	{
		return length;
	}
}
