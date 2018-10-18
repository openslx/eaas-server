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

import java.util.concurrent.TimeUnit;


/** Helper class for time conversions. */
public final class TimeUtils
{
	/**
	 * Convert the given time duration between the given units.
	 * @param duration The time duration to convert.
	 * @param srcunit The source time-unit to convert from.
	 * @param dstunit The destination time-unit to convert to.
	 * @return The converted time duration.
	 * 
	 * @see {@link TimeUnit#convert(long, TimeUnit)}
	 */
	public static long convert(long duration, TimeUnit srcunit, TimeUnit dstunit)
	{
		return dstunit.convert(duration, srcunit);
	}
}
