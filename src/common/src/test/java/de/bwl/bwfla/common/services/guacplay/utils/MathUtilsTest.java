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
import de.bwl.bwfla.common.services.guacplay.util.MathUtils;


public class MathUtilsTest extends BaseTest
{
	private static final int NUMCOUNT = 10000000;
	
	private static final long[] LONGS = new long[NUMCOUNT];
	private static final int[]  INTEGERS  = new int[NUMCOUNT];
	static {
		// Generating random numbers
		final Random random = new Random();
		for (int i = 0; i < NUMCOUNT; ++i) {
			INTEGERS[i] = random.nextInt();
			LONGS[i] = random.nextLong();
		}
	}
	
	
	@Test
	public void testAbs()
	{
		log.info("Testing MathUtils.abs(int)...");
		
		for (int i = 0; i < NUMCOUNT; ++i) {
			int number = INTEGERS[i];
			int expected = Math.abs(number);
			int actual = MathUtils.abs(number);
			Assert.assertEquals(expected, actual);
		}
		
		log.info("Testing MathUtils.abs(long)...");
		
		for (int i = 0; i < NUMCOUNT; ++i) {
			long number = LONGS[i];
			long expected = Math.abs(number);
			long actual = MathUtils.abs(number);
			Assert.assertEquals(expected, actual);
		}
		
		this.markAsPassed();
	}
	
	@Test
	public void testCneg()
	{
		log.info("Testing MathUtils.cneg(int, int)...");
		
		for (int i = 0; i < NUMCOUNT; ++i) {
			int number = INTEGERS[i];
			int expected = number;
			int actual = MathUtils.cneg(number, MathUtils.FALSE);
			Assert.assertEquals(expected, actual);
			
			expected = -number;
			actual = MathUtils.cneg(number, MathUtils.TRUE);
			Assert.assertEquals(expected, actual);
		}
		
		log.info("Testing MathUtils.cneg(long, long)...");
		
		for (int i = 0; i < NUMCOUNT; ++i) {
			long number = LONGS[i];
			long expected = number;
			long actual = MathUtils.cneg(number, MathUtils.FALSE);
			Assert.assertEquals(expected, actual);
			
			expected = -number;
			actual = MathUtils.cneg(number, MathUtils.TRUE);
			Assert.assertEquals(expected, actual);
		}
		
		this.markAsPassed();
	}
	
	@Test
	public void testSign()
	{
		log.info("Testing MathUtils.sign(int)...");
		
		for (int i = 0; i < NUMCOUNT; ++i) {
			int number = INTEGERS[i];
			int expected = (number < 0) ? 1 : 0;
			int actual = MathUtils.sign(number);
			Assert.assertEquals(expected, actual);
		}
		
		log.info("Testing MathUtils.sign(long)...");
		
		for (int i = 0; i < NUMCOUNT; ++i) {
			long number = LONGS[i];
			long expected = (number < 0L) ? 1L : 0L;
			long actual = MathUtils.sign(number);
			Assert.assertEquals(expected, actual);
		}
		
		this.markAsPassed();
	}
	
	@Test
	public void testMod2()
	{
		log.info("Testing MathUtils.mod2(int,int)...");
		
		for (int i = 0; i < NUMCOUNT; ++i) {
			final int number = MathUtils.abs(INTEGERS[i]);
			for (int j = 0; j < 32; ++j) {
				int expected = number % (1 << j);
				int actual = MathUtils.mod2(number, j);
				Assert.assertEquals(expected, actual);
			}
		}
		
		log.info("Testing MathUtils.mod2(long,long)...");
		
		for (int i = 0; i < NUMCOUNT; ++i) {
			final long number = MathUtils.abs(LONGS[i]);
			for (long j = 0; j < 64; ++j) {
				long expected = number % (1L << j);
				long actual = MathUtils.mod2(number, j);
				Assert.assertEquals(expected, actual);
			}
		}
		
		this.markAsPassed();
	}
}
