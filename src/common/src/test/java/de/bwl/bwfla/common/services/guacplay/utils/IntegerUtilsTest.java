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

package de.bwl.bwfla.common.services.guacplay.utils;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import de.bwl.bwfla.common.services.guacplay.BaseTest;
import de.bwl.bwfla.common.services.guacplay.util.LongUtils;
import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;


public class IntegerUtilsTest extends BaseTest
{
	private static final int NUM_ITERATIONS = 10000000;
	
	
	@Test
	public void testToBase16()
	{
		final StringBuffer strbuf = new StringBuffer(64);
		final Random random = new Random();
		
		log.info("Running " + IntegerUtilsTest.class.getSimpleName() + "...");
		
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			final long number = random.nextLong();
			LongUtils.toBase16(number, strbuf);
			
			final String exp = "0x" + Long.toHexString(number).toUpperCase();
			final String cur = strbuf.toString();
			if (!cur.contentEquals(exp))
				Assert.fail("" + cur + " != " + exp);
			
			strbuf.clear();
		}

		this.markAsPassed();
	}
}
