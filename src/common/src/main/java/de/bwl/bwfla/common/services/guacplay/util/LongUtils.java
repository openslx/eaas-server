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


/** Optimized routines for 64-bit integers. */
public final class LongUtils
{
	/**
	 * Parse and return a 64-bit signed integer, in decimal notation.
	 * @param data The buffer containing the integer's digits.
	 * @param offset The offset of the first digit.
	 * @param length The number of digits to parse.
	 * @return The parsed integer.
	 */
	public static long fromBase10(char[] data, int offset, int length)
	{
		long negate = MathUtils.FALSE;
		if (data[offset] == '-') {
			// It is a negative number
			negate = MathUtils.TRUE;
			++offset;
			--length;
		}
		
		long number = 0L;
		
		// Parse number from the buffer
		for (int offmax = offset + length; offset < offmax; ++offset)
			number = LongUtils.append(number, data[offset]);
		
		// Handle the sign, when negative
		return MathUtils.cneg(number, negate);
	}
	
	/**
	 * Converts the specified 64-bit number to a string in hexa-decimal notation.
	 * @param number The 64-bit integer to convert.
	 * @param strbuf The output-buffer for the result.
	 */
	public static void toBase16(long number, StringBuffer strbuf)
	{
		final long mask = 0xF;
		final long step = 4;
		long shift = 64 - step;
		int index = 0;
		
		// Skip all leading zeros
		do {
			index = (int)((number >>> shift) & mask);
			if (index != 0)
				break;
			shift -= step;
		} while (shift > 0L);
		
		// Hex prefix
		strbuf.append("0x");
		
		// Append the digits
		do {
			index = (int)((number >>> shift) & mask);
			strbuf.append(IntegerUtils.digit(index));
			shift -= step;
		} while (shift >= 0L);
	}
	
	/**
	 * Update a 64-bit integer with a new digit.
	 * @param number The number to update.
	 * @param digit The new digit to add.
	 * @return Updated integer.
	 */
	public static long append(long number, char digit)
	{
		IntegerUtils.ensureValidDigit(digit);
		
		// Update the number with valid digit.
		
		// Replace the multiplication by 10 with faster shifts and add operations:
		//     number * 10 = number * (8 + 2) = (number << 3) + (number << 1)
		number = (number << 3L) + (number << 1L);
		return (number + (long)(digit - '0'));
	}

	/**
	 * Update a 64-bit integer with a new digit. <p/>
	 * <b>NOTE:</b> The character to append is assumed to be a valid digit in the range 0-9!
	 * @param number The number to update.
	 * @param digit The new digit to add.
	 * @return Updated integer.
	 */
	public static long appendUnsafe(long number, char digit)
	{
		// Update the number with valid digit.
		
		// Replace the multiplication by 10 with faster shifts and add operations:
		//     number * 10 = number * (8 + 2) = (number << 3) + (number << 1)
		number = (number << 3L) + (number << 1L);
		return (number + (long)(digit - '0'));
	}
	
	/** Calculate the length of the string representing the specified positive number! */
	public static int getStringLength(final long number)
	{
		assert(number >= 0L) : "The number must be positive!";
		
		long n = 10L;
		int length = 1;
		
		 // Find the length
		while ((length < 19) && (n <= number)) {
			n = (n << 3) + (n << 1);  // == 10*n
			++length;
		}
		
		return length;
	}
}
