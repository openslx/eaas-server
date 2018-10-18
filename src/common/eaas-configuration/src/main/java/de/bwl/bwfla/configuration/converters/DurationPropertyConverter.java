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

package de.bwl.bwfla.configuration.converters;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;


/** A converter for strings of the form <i>"25 seconds"</i> to durations */
public class DurationPropertyConverter implements PropertyConverter<Duration>
{
	private final DurationParser parser;

	
	public DurationPropertyConverter()
	{
		this.parser = new DurationParser();
	}
	
	@Override
	public Duration convert(String value, ConversionContext context)
	{
		return Duration.ofMillis(parser.parse(value, TimeUnit.MILLISECONDS));
	}
	
	
	/** A parser for strings of the form <i>"25 seconds"</i>. */
	private class DurationParser
	{
	    private final Map<String, TimeUnit> units;
	    private final Pattern pattern;

	    
	    public DurationParser()
	    {
	        this.units = new HashMap<String, TimeUnit>();
	        this.pattern = Pattern.compile("(\\d+)[ \\t]*([a-z]*)");
	        
	        for (String suffix : new String[] { "ms", "msec", "msecs", "millisecond", "milliseconds" })
	            units.put(suffix, TimeUnit.MILLISECONDS);
	        
	        for (String suffix : new String[] { "s", "sec", "secs", "second", "seconds" })
	            units.put(suffix, TimeUnit.SECONDS);
	        
	        for (String suffix : new String[] { "m", "min", "mins", "minute", "minutes" })
	            units.put(suffix, TimeUnit.MINUTES);
	        
	        for (String suffix : new String[] { "h", "hour", "hours" })
	            units.put(suffix, TimeUnit.HOURS);
	    }
	    
	    public long parse(String value, TimeUnit outunit)
	    {
	        if (value == null || value.isEmpty())
	            throw new IllegalArgumentException("Duration value is null or empty!");
	        
	        // Values are expected to be in the form:
	        //     <number> <unit>
	        //     <number><unit>
	        //
	        // Examples:
	        //     5 secs (or 5s)
	        //     1 minute (or 1m)
	        
	        final Matcher matcher = pattern.matcher(value);
	        if (!matcher.matches())
	            throw new IllegalArgumentException("Duration value is malformed: " + value);
	        
	        final long duration = Long.parseLong(matcher.group(1));
	        final TimeUnit unit = units.get(matcher.group(2));
	        if (unit == null)
	            throw new IllegalArgumentException("Duration unit is unknown: " + matcher.group(2));
	        
	        return outunit.convert(duration, unit);
	    }
	}
}
