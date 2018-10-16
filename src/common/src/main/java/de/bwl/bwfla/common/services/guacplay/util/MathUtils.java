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


public final class MathUtils
{
	/** Mask for converting the unsigned byte to int. */
	public static final int MASK_UNSIGNED_BYTE = 0xFF;

	/** Mask for converting the unsigned int to long. */
	public static final long MASK_UNSIGNED_INT = 0xFFFFFFFFL;
	
	public static final int FALSE = 0;
	public static final int TRUE  = 1;
	
	
	/** Branchless version of {@link java.lang.Math#abs(int)}. */
	public static int abs(int number)
	{
		// From 'Bit Twiddling Hacks':
		// http://graphics.stanford.edu/~seander/bithacks.html#IntegerAbs
		
		final int mask = number >> 31;
		return ((number + mask) ^ mask);
	}
	
	/** Branchless version of {@link java.lang.Math#abs(long)}. */
	public static long abs(long number)
	{
		// From 'Bit Twiddling Hacks':
		// http://graphics.stanford.edu/~seander/bithacks.html#IntegerAbs
		
		final long mask = number >> 63;
		return ((number + mask) ^ mask);
	}
	
	/**
	 * Conditionally negates a number without branching.
	 * Equivalent to the expression: <p>
	 * <code> (negate == 1) ? -number : number; </code>
	 * @param number The number to negate.
	 * @param negate The flag, indicating whether to negate (=1) or not (=0).
	 */
	public static int cneg(int number, int negate)
	{
		// From 'Bit Twiddling Hacks':
		// http://graphics.stanford.edu/~seander/bithacks.html#ConditionalNegate
		return ((number ^ -negate) + negate);
	}
	
	/**
	 * Conditionally negates a number without branching.
	 * Equivalent to the expression: <p>
	 * <code> (negate == 1) ? -number : number; </code>
	 * @param number The number to negate.
	 * @param negate The flag, indicating whether to negate (=1) or not (=0).
	 */
	public static long cneg(long number, long negate)
	{
		// From 'Bit Twiddling Hacks':
		// http://graphics.stanford.edu/~seander/bithacks.html#ConditionalNegate
		return ((number ^ -negate) + negate);
	}
	
	/** Returns 1 if the specified number is negative, else 0. */
	public static int sign(int number)
	{
		return (number >>> 31);
	}
	
	/** Returns 1 if the specified number is negative, else 0. */
	public static long sign(long number)
	{
		return (number >>> 63);
	}
	
	/**
	 * Calculates the following expression: <p> <code> number % 2^pow </code> <p>
	 * The values must satisfy following constraints: (number >= 0) and (0 <= pow < 32)
	 */
	public static int mod2(int number, int pow)
	{
		int tmp = (number >> pow) << pow;
		return (number - tmp);
	}
	
	/** 
	 * Calculates the following expression: <p> <code> number % 2^pow </code> <p>
	 * The values must satisfy following constraints: (number >= 0) and (0 <= pow < 64) 
	 */
	public static long mod2(long number, long pow)
	{
		long tmp = (number >> pow) << pow;
		return (number - tmp);
	}
	
	/** Interprets the value of the specified byte as unsigned byte. */
	public static int asUByte(byte number)
	{
		return (number & MASK_UNSIGNED_BYTE);
	}
	
	/** Interprets the value of the specified int as unsigned int. */
	public static long asUInt(int number)
	{
		return (number & MASK_UNSIGNED_INT);
	}
	
	/** Returns true if the specified value is power of 2, else false. */
	public static boolean isPowerOf2(int value)
	{
		// From 'Bit Twiddling Hacks':
		// http://graphics.stanford.edu/~seander/bithacks.html#DetermineIfPowerOf2
		return (value != 0) && ((value & (value - 1)) == 0);
	}
	
	/** Returns true if the specified value is power of 2, else false. */
	public static boolean isPowerOf2(long value)
	{
		// From 'Bit Twiddling Hacks':
		// http://graphics.stanford.edu/~seander/bithacks.html#DetermineIfPowerOf2
		return (value != 0L) && ((value & (value - 1L)) == 0L);
	}
}
