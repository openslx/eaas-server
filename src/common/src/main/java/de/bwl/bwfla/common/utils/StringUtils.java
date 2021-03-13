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

package de.bwl.bwfla.common.utils;

import java.util.Random;


public class StringUtils
{
	/** Generate random string of given length */
	public static String random(int length)
	{
		return StringUtils.random(length, new Random());
	}

	/** Generate random string of given length */
	public static String random(int length, Random random)
	{
		final var builder = new StringBuilder(length);
		final var alphabet = "0123456789ABCDEFGHKMNPQRSTUVWXYZ";
		random.ints(length, 0, alphabet.length())
				.forEach((i) -> builder.append(alphabet.charAt(i)));

		return builder.toString();
	}

	/** Return a human-readable size with unit */
	public static String toHumanSize(long size)
	{
		if (size < 1024)
			return (size == 1) ? "1 byte" : size + " bytes";

		// 1024 == 2^10 == 10 bits/unit
		// --> use num. of leading zeros to compute unit!
		final int upos = (63 - Long.numberOfLeadingZeros(size)) / 10;
		final double fpsize = (double) size / (double) (1L << (upos * 10));
		return String.format("%.2f %cB", fpsize, "xKMGTPE".charAt(upos));
	}
}
