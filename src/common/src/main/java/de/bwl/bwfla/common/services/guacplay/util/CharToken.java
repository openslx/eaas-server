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


/** A simple class, representing a char-token. */
public final class CharToken
{
	private char[] data;
	private int start;
	private int end;
	
	
	/** Constructor */
	public CharToken()
	{
		this(null, -1, -1);
	}
	
	/** Constructor */
	public CharToken(char[] data, int start, int end)
	{
		this.data = data;
		this.start = start;
		this.end = end;
	}
	
	/** Update the token with new content. */
	public void set(char[] data, int start, int end)
	{
		this.data = data;
		this.start = start;
		this.end = end;
	}
	
	/** Returns the underlying array. */
	public char[] array()
	{
		 return data;
	}
	
	/** Returns the token's start offset. */
	public int start()
	{
		return start;
	}
	
	/** Returns the token's end offset. */
	public int end()
	{
		return end;
	}
	
	/** Returns the token's length. */
	public int length()
	{
		return (end - start);
	}
	
	/** Converts this token to a string. */
	public String toString()
	{
		return new String(data, start, end - start);
	}
	
	/** Converts this token to an integer. */
	public int toInt()
	{
		return IntegerUtils.fromBase10(data, start, end - start);
	}
	
	/** Converts this token to an integer. */
	public long toLong()
	{
		return LongUtils.fromBase10(data, start, end - start);
	}
}
