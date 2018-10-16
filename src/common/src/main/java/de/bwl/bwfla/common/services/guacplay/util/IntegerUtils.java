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


/** Optimized routines for 32-bit integers. */
public final class IntegerUtils
{
	/** All valid digits. */
	private static final char[] DIGITS = "0123456789ABCDEF".toCharArray();
	
	/** Lookup table for calculating the integer-string lengths. */
	private static final int[] SIZES =
			{ 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };
	
	
	/**
	 * Returns the digit corresponding to specified index.
	 * @param index Must satisfy: (0 <= index < 16)
	 */
	public static char digit(int index)
	{
		assert(index >= 0 && index < 16) : "Index must be in the range 0-15!";
		return DIGITS[index];
	}
	
	/**
	 * Parse and return a 32-bit signed integer, in decimal notation.
	 * @param data The buffer containing the integer's digits.
	 * @param offset The offset of the first digit.
	 * @param length The number of digits to parse.
	 * @return The parsed integer.
	 */
	public static int fromBase10(char[] data, int offset, int length)
	{
		int negate = MathUtils.FALSE;
		if (data[offset] == '-') {
			// It is a negative number
			negate = MathUtils.TRUE;
			++offset;
			--length;
		}
		
		int number = 0;
		
		// Parse number from the buffer
		for (int offmax = offset + length; offset < offmax; ++offset)
			number = IntegerUtils.append(number, data[offset]);
		
		// Handle the sign, when negative
		return MathUtils.cneg(number, negate);
	}
	
	/**
	 * Update a 32-bit integer with a new digit.
	 * @param number The number to update.
	 * @param digit The new digit to add.
	 * @return Updated integer.
	 */
	public static int append(int number, char digit)
	{
		IntegerUtils.ensureValidDigit(digit);
		
		// Update the number with valid digit.
		
		// Replace the multiplication by 10 with faster shifts and add operations:
		//     number * 10 = number * (8 + 2) = (number << 3) + (number << 1)
		number = (number << 3) + (number << 1);
		return (number + (digit - '0'));
	}
	
	/**
	 * Update a 32-bit integer with a new digit. <p/>
	 * <b>NOTE:</b> The character to append is assumed to be a valid digit in the range 0-9!
	 * @param number The number to update.
	 * @param digit The new digit to add.
	 * @return Updated integer.
	 */
	public static int appendUnsafe(int number, char digit)
	{
		// Replace the multiplication by 10 with faster shifts and add operations:
		//     number * 10 = number * (8 + 2) = (number << 3) + (number << 1)
		number = (number << 3) + (number << 1);
		return (number + (digit - '0'));
	}
	
	/** Calculate the length of the string representing the specified positive number */
	public static int getStringLength(final int number)
	{
		assert(number >= 0) : "The number must be positive!";
		
		int length = 0;
		
		 // Find the length
		while (number > SIZES[length])
			++length;
		
		return (length + 1);
	}
	
	/** Check, that the specified character is a valid digit. */
	public static void ensureValidDigit(char character) throws IllegalArgumentException
	{
		// Invalid digit character read?
		if ((character < '0') || (character > '9')) {
			String message = "A digit was expected, but found: " + character;
			throw new IllegalArgumentException(message);
		}
	}
}