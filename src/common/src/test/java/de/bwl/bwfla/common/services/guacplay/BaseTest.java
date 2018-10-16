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

package de.bwl.bwfla.common.services.guacplay;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BaseTest
{
	/** Logger instance */
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected static boolean TESTS_PASSED = true;
	private boolean curTestPassed = false;
	
	
	@Before
	public void setUpBefore()
	{
		curTestPassed = false;
	}
	
	@After
	public void tearDownAfter()
	{
		TESTS_PASSED &= curTestPassed;
		
		if (curTestPassed)
			log.info("Test passed!");
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		final Logger log = LoggerFactory.getLogger(BaseTest.class);
		
		if (TESTS_PASSED)
			log.info("ALL TESTS PASSED!");
		else log.warn("Some tests failed!");
	}
	
	public final void markAsPassed()
	{
		curTestPassed = true;
	}
}
