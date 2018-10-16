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

import java.nio.CharBuffer;


/**
 * A custom StringBuffer implementing a subset of features available in 
 * {@link java.lang.StringBuilder}. The main difference is, that this class 
 * allows a direct access to the underlying char array without any copying! 
 */
public class StringBuffer
{
	private CharBuffer buffer;
	
	/** Constructs a new StringBuffer with the specified capacity. */
	public StringBuffer(int capacity)
	{
		this.buffer = CharBuffer.allocate(capacity);
	}
	
	/** Appends a char. */
	public void append(char c)
	{
		this.grow(1);
		buffer.put(c);
	}
	
	/** Appends a char-array. */
	public void append(char[] src)
	{
		this.append(src, 0, src.length);
	}
	
	/** Appends the specified part of the char-array. */
	public void append(char[] src, int offset, int length)
	{
		this.grow(length);
		buffer.put(src, offset, length);
	}
	
	/** Appends a String. */
	public void append(String str)
	{
		this.grow(str.length());
		buffer.put(str);
	}
	
	/** Appends the specified part of the string. */
	public void append(String str, int start, int end)
	{
		this.grow(str.length());
		buffer.put(str, start, end);
	}
	
	/** Appends a 32-bit integer. */
	public void append(int intnum)
	{
		// The following implementation is based on the code from java.lang.Integer.toString().
		//
		// OPTIMIZATION #1:
		//     To avoid the costly division instruction in the calculation of 'quotient = n / 10' both
		//     implementations use the technique described by T. Gralund and P. Montgomery in the paper:
		//     "Division by Invariant Integers using Multiplication", from 1994.
		//
		// OPTIMIZATION #2:
		//     The multiplications by 10 for computing the remainder are replaced with faster shifts + add:
		//     x * 10 = x * (8 + 2) = (x * 8) + (x * 2) = (x << 3) + (x << 1)
		
		if (intnum == Integer.MIN_VALUE) {
			this.append("-2147483648");
			return;
		}
		
		char sign = 0;
		int length;
		
		if (intnum < 0) {
			// Negative integer
			sign = '-';
			intnum = MathUtils.abs(intnum);
			length = IntegerUtils.getStringLength(intnum) + 1;
		}
		else {
			// Positive integer
			length = IntegerUtils.getStringLength(intnum);
		}
		
		this.grow(length);
		
		// Position in the buffer for inserting digits
		int bufpos = buffer.position() + length;
		buffer.position(bufpos);  // Reposition
		
		// Can a multiplication result be represented by 32-bit integer?
		if (intnum < 65536) {
			// Yes, construct the string using int arithmetics only!
			// From paper: m = (2^18 + 1) / 5 = 52429, shift = 3
			// Since the upper part of the result needed: shift = shift + 16
			final int m = 52429;
			final int shift = 3 + 16;
			int quotient, remainder;
			do {
				// quotient = number / 10
				quotient = (m * intnum) >>> shift;
				
				// remainder = number - (quotient * 10)
				remainder = intnum - ((quotient << 3) + (quotient << 1));
				
				// Fill in the digit and proceed with next
				buffer.put(--bufpos, IntegerUtils.digit(remainder));
				intnum = quotient;
			} while (intnum > 0);
		}
		else {
			// The result can be bigger than 2^32, use long arithmetics!
			// From paper: m = (2^34 + 1) / 5 = 3435973837, shift = 3
			// Since the upper part of the result needed: shift = shift + 32
			final long m = 3435973837L;
			final int shift = 3 + 32;
			long longnum = (long) intnum;
			long quotient, remainder;
			do {
				// quotient = number / 10
				quotient = (m * longnum) >>> shift;
				
				// remainder = number - (quotient * 10)
				remainder = longnum - ((quotient << 3) + (quotient << 1));
				
				// Fill in the digit and proceed with next
				buffer.put(--bufpos, IntegerUtils.digit((int) remainder));
				longnum = quotient;
			} while (longnum > 0L);
		}
		
		// Append sign if needed
		if (sign != 0)
			buffer.put(--bufpos, sign);
	}
	
	/** Appends a 64-bit integer. */
	public void append(long longnum)
	{
		// The following implementation is based on the code from java.lang.Integer.toString().
		//
		// OPTIMIZATION #1:
		//     To avoid the costly division instruction in the calculation of 'quotient = n / 10' both
		//     implementations use the technique described by T. Gralund and P. Montgomery in the paper:
		//     "Division by Invariant Integers using Multiplication", from 1994.
		//
		// OPTIMIZATION #2:
		//     The multiplications by 10 for computing the remainder are replaced with faster shifts + add:
		//     x * 10 = x * (8 + 2) = (x * 8) + (x * 2) = (x << 3) + (x << 1)
		
		long absnum = MathUtils.abs(longnum);
		if (absnum > Integer.MAX_VALUE) {
			// The result of the multiplication can not be represented by an
			// 64-bit integer, fallback to the built-in Long.tString() method!
			this.append(Long.toString(longnum));
			return;
		}
		
		char sign = 0;
		int length;
		
		if (longnum < 0) {
			// Negative integer
			sign = '-';
			length = LongUtils.getStringLength(longnum) + 1;
		}
		else {
			// Positive integer
			length = LongUtils.getStringLength(longnum);
		}
		
		this.grow(length);
		
		// Position in the buffer for inserting digits
		int bufpos = buffer.position() + length;
		buffer.position(bufpos);  // Reposition
		
		// From paper: m = (2^34 + 1) / 5 = 3435973837, shift = 3
		// Since the upper part of the result needed: shift = shift + 32
		final long m = 3435973837L;
		final int shift = 3 + 32;
		long quotient, remainder;
		do {
			// quotient = number / 10
			quotient = (m * absnum) >>> shift;
			
			// remainder = number - (quotient * 10)
			remainder = absnum - ((quotient << 3) + (quotient << 1));
			
			// Fill in the digit and proceed with next
			buffer.put(--bufpos, IntegerUtils.digit((int) remainder));
			absnum = quotient;
		} while (absnum > 0L);
		
		// Append sign if needed
		if (sign != 0)
			buffer.put(--bufpos, sign);
	}
	
	/** Returns the underlying char-array. */
	public char[] array()
	{
		return buffer.array();
	}
	
	/** Returns the string's length in the underlying char-array. */
	public int length()
	{
		return buffer.position();
	}
	
	/** Returns the underlying {@link CharBuffer}. */
	public CharBuffer buffer()
	{
		buffer.flip();
		return buffer;
	}

	/** Resets the StringBuffer, without clearing the actual content. */
	public void clear()
	{
		buffer.clear();
	}
	
	@Override
	public String toString()
	{
		return new String(buffer.array(), 0, this.length());
	}
	
	public char[] toCharArray()
	{
		final int length = this.length();
		final char[] array = new char[length];
		System.arraycopy(buffer.array(), 0, array, 0, length);
		return array;
	}
	
	
	/* =============== Internal Methods =============== */
	
	/** Resize the internal buffer, when needed. */
	private void grow(int delta)
	{
		// Growing needed?
		if (buffer.remaining() >= delta)
			return;  // No

		// Yes, allocate a new bigger buffer
		final CharBuffer curbuf = buffer;
		final int newcap = Math.max(curbuf.position() + delta, curbuf.capacity() << 1);
		buffer = CharBuffer.allocate(newcap);
		
		// Copy data into new buffer
		curbuf.flip();
		buffer.put(curbuf);
	}
}
