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

package com.openslx.eaas.common.config.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** A parser for strings representing memory units. */
public class MemoryUnitParser
{
	private final Map<String, Function<Long, Long>> units;
	private final Pattern pattern;

	
	public MemoryUnitParser()
	{
		this.units = new HashMap<String, Function<Long, Long>>();
		this.pattern = Pattern.compile("(\\d+)[ \\t]*([a-zA-Z]*)");
		
		for (String suffix : new String[] { "k", "K", "kb", "KB" })
			units.put(suffix, (bytes) -> bytes * 1024 );
		
		for (String suffix : new String[] { "m", "M", "mb", "MB" })
			units.put(suffix, (bytes) -> bytes * 1024 * 1024 );
		
		for (String suffix : new String[] { "g", "G", "gb", "GB" })
			units.put(suffix, (bytes) -> bytes * 1024 * 1024 * 1024 );
	}
	
	/** Returns value in bytes. */
	public long parse(String value)
	{
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException("Memory value is null or empty!");
		
		// Values are expected to be in the form:
		//     <number> <unit>
		//     <number><unit>
		//     +inf
		//
		// Examples:
		//     128 mb (or 128M)
		//     2GB (or 2g)
		
		if (value.contentEquals("inf") || value.contentEquals("+inf"))
			return Long.MAX_VALUE;
		
		final Matcher matcher = pattern.matcher(value);
		if (!matcher.matches())
			throw new IllegalArgumentException("Memory value is malformed: " + value);
		
		final long bytes = Long.parseLong(matcher.group(1));
		Function<Long, Long> unit = units.get(matcher.group(2));
		if (unit == null)
			unit = (input) -> input;
		
		return unit.apply(bytes);
	}
}
