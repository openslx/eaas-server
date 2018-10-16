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


/** A wrapper for char-arrays. */
public final class CharArrayWrapper extends ArrayWrapper
{
	private char[] array;
	
	
	/** Constructor */
	public CharArrayWrapper()
	{
		this.array = null;
	}

	/** Reset this instance. */
	public void reset()
	{
		this.set(null, -1, -1);
	}
	
	/** Set the underlying array. */
	public void set(char[] array)
	{
		this.set(0, array.length);
		this.array = array;
	}
	
	/** Set the underlying array. */
	public void set(char[] array, int offset, int length)
	{
		this.set(offset, length);
		this.array = array;
	}
	
	/** Returns the underlying array. */
	public char[] array()
	{
		return array;
	}
	
	public char[] toCharArray()
	{
		if ((offset == 0) && (array.length == length))
			return array;
		
		char[] newarray = new char[length];
		System.arraycopy(array, offset, newarray, 0, length);
		return newarray;
	}
}
