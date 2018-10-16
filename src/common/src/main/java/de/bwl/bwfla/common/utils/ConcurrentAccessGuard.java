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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import de.bwl.bwfla.common.exceptions.ConcurrentAccessException;


/** Simple helper class for detecting concurrent access to objects. */
public class ConcurrentAccessGuard
{
	private static final String UNDEFINED_OPERATION = "";
	
	private final AtomicInteger value;
	private String curmethod;

	
	public ConcurrentAccessGuard()
	{
		this(0);
	}
	
	public ConcurrentAccessGuard(int initValue)
	{
		this.value = new AtomicInteger(initValue);
		this.curmethod = "";
	}

	public void prolog(String method) throws ConcurrentAccessException
	{
		this.prolog(0, 1, method);
	}
	
	public int epilog()
	{
		return this.epilog(0);
	}
	
	public void prolog(int expval, int newval, String method) throws ConcurrentAccessException
	{
		if (!value.compareAndSet(expval, newval)) {
			String message = "Concurrent modification/access detected! "
					+ curmethod + " is still in-flight, during which "
					+ "the requested " + method + " cannot be invoked!";
			
			throw new ConcurrentAccessException(message);
		}
		
		curmethod = method;
	}
	
	public boolean force(int expval, int newval, String method)
	{
		final int curval = value.getAndSet(newval);
		if (curval == expval)
			return true;
		
		String message = "Concurrent modification/access detected! "
				+ curmethod + " is still in-flight, during which "
				+ "the requested " + method + " was invoked!";

		Logger log = Logger.getLogger("ConcurrentAccessGuard");
		log.warning(message);
		return false;
	}
	
	public int epilog(int update)
	{
		curmethod = UNDEFINED_OPERATION;
		return value.getAndSet(update);
	}
}
