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


/** A simple class for wrapping plain char arrays. */
public class CharArrayBuffer extends ArrayBuffer
{
	private char[] array;
	
	/** Constructor */
	public CharArrayBuffer()
	{
		super();
		this.array = null;
	}
	
	/** Constructor */
	public CharArrayBuffer(char[] array)
	{
		this.set(array, 0, array.length);
	}
	
	/** Constructor */
	public CharArrayBuffer(char[] array, int offset, int length)
	{
		this.set(array, offset, length);
	}
	
	/** Update the wrapper with new content. */
	public void set(char[] array)
	{
		this.set(array, 0, array.length);
	}
	
	/** Update the wrapper with new content. */
	public void set(char[] array, int offset, int length)
	{
		this.array = array;
		super.set(offset, length);
	}
	
	/** Returns the wrapped array. */
	public char[] array()
	{
		 return array;
	}
	
	/** Relative get operation. */
	public char get()
	{
		char current = array[position];
		++position;
		return current;
	}
	
	/** Relative get operation with bounds-checking. */
	public char getChecked()
	{
		super.checkUnderflow(position);
		return this.get();
	}
	
	/** Absolute get operation. */
	public char get(int pos)
	{
		return array[pos];
	}
	
	/** Absolute get operation with bounds-checking. */
	public char getChecked(int pos)
	{
		super.checkUnderflow(pos);
		return this.get(pos);
	}
}
