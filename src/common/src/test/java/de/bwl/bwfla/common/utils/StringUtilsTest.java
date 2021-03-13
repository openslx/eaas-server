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

import org.junit.Assert;
import org.junit.Test;


public class StringUtilsTest
{
	@FunctionalInterface
	interface HumanSizeChecker
	{
		void check(String string, double value, long unit);
	}

	@Test
	public void testHumanSize()
	{
		final long bytes = 1L;
		final long kb = 1024L * bytes;
		final long mb = 1024L * kb;
		final long gb = 1024L * mb;
		final long tb = 1024L * gb;
		final long pb = 1024L * tb;

		final HumanSizeChecker checker = (str, value, unit) -> {
			final var size = (long)(value * unit);
			Assert.assertEquals(str, StringUtils.toHumanSize(size));
		};

		checker.check("1 byte", 1, bytes);
		checker.check("999 bytes", 999, bytes);
		checker.check("1023 bytes", 1023, bytes);

		checker.check("1.00 KB", 1, kb);
		checker.check("4.99 KB", 4.99, kb);
		checker.check("1023.00 KB", 1023, kb);

		checker.check("1.00 MB", 1, mb);
		checker.check("2.50 MB", 2.5, mb);
		checker.check("169.97 MB", 169.97, mb);
		checker.check("1023.93 MB", 1023.93, mb);

		checker.check("1.00 GB", 1, gb);
		checker.check("33.78 GB", 33.78, gb);
		checker.check("1023.99 GB", 1023.99, gb);

		checker.check("1.00 TB", 1, tb);
		checker.check("49.25 TB", 49.25, tb);
		checker.check("1023.99 TB", 1023.99, tb);

		checker.check("1.00 PB", 1, pb);
		checker.check("728.09 PB", 728.09, pb);
		checker.check("1023.99 PB", 1023.99, pb);
	}
}
