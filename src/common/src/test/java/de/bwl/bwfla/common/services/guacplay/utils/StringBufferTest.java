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
import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;


public class StringBufferTest extends BaseTest
{
	private static final int NUM_ITERATIONS = 50000000;
	
	
	@Test
	public void testAppendInt()
	{
		final StringBuffer strbuf = new StringBuffer(32);
		final Random random = new Random();
		
		log.info("Test appending of 32-bit integers...");
		
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			final int number = random.nextInt();

			strbuf.clear();
			strbuf.append(number);

			final String str1 = Integer.toString(number);
			final String str2 = strbuf.toString();
			if (!str1.contentEquals(str2)) {
				System.out.println("Error found! Expected: " + str1 + ", Current: " + str2);
				System.out.println("Error found! Expected length: " + str1.length() + ", Current length: " + str2.length());
				Assert.fail();
			}
		}

		this.markAsPassed();
	}
	
	@Test
	public void testAppendLong()
	{
		final StringBuffer strbuf = new StringBuffer(32);
		final Random random = new Random();

		log.info("Test appending of 64-bit integers...");
		
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			final long number = random.nextLong();

			strbuf.clear();
			strbuf.append(number);

			final String str1 = Long.toString(number);
			final String str2 = strbuf.toString();
			if (!str1.contentEquals(str2)) {
				System.out.println("Error found! Expected: " + str1 + ", Current: " + str2);
				System.out.println("Error found! Expected length: " + str1.length() + ", Current length: " + str2.length());
				Assert.fail();
			}
		}

		this.markAsPassed();
	}
}
