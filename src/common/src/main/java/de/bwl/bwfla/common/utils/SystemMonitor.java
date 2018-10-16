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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** A helper class for monitoring system-resources. */
public class SystemMonitor
{
	private final RandomAccessFile loadavg;
	private final RandomAccessFile meminfo;
	private final Map<ValueID, String> values;
	private int startIndex;
	
	/** IDs of the monitored values */
	public static enum ValueID
	{
		LOAD_AVERAGE_1MIN,   /* System-load averaged over 1 min. */
		LOAD_AVERAGE_5MIN,   /* System-load averaged over 5 min. */
		LOAD_AVERAGE_15MIN,  /* System-load averaged over 15 min. */
		MEMORY_TOTAL,        /* Total usable RAM. */
		MEMORY_FREE,         /* Amount of free memory. */
		MEMORY_BUFFERS,      /* Temporary storage for raw disk blocks. */
		MEMORY_CACHED,       /* In-memory cache for files read from the disk. */
		MEMORY_ACTIVE        /* Memory used more recently, usually not reclaimed unless necessary. */
	}
	
	
	/** Constructor */
	public SystemMonitor() throws FileNotFoundException
	{
		this.loadavg = new RandomAccessFile("/proc/loadavg", "r");
		this.meminfo = new RandomAccessFile("/proc/meminfo", "r");
		this.values = new HashMap<ValueID, String>();
	}
	
	/** Updates the monitored values. */
	public void update() throws IOException
	{
		// Update loadavg fields...
		{
			final String data = this.readLoadAvgFile();
			values.put(ValueID.LOAD_AVERAGE_1MIN, this.nextLoadField(data));
			values.put(ValueID.LOAD_AVERAGE_5MIN, this.nextLoadField(data));
			values.put(ValueID.LOAD_AVERAGE_15MIN, this.nextLoadField(data));
		}
		
		// Update meminfo fields...
		{
			meminfo.seek(0);
			
			values.put(ValueID.MEMORY_TOTAL, this.nextMemInfoField(0));
			values.put(ValueID.MEMORY_FREE, this.nextMemInfoField(0));
			values.put(ValueID.MEMORY_BUFFERS, this.nextMemInfoField(0));
			values.put(ValueID.MEMORY_CACHED, this.nextMemInfoField(0));
			values.put(ValueID.MEMORY_ACTIVE, this.nextMemInfoField(1));
		}
	}
	
	/**
	 * Returns a single monitored value.
	 * @param id The ID of the value to return.
	 * @return The value as string. If the value is not valid, then an empty string is returned.
	 */
	public String getValue(ValueID id)
	{
		String value = values.get(id);
		return (value != null) ? value : "";
	}
	
	/**
	 * Returns specific monitored values.
	 * @param ids The IDs of the values to return.
	 * @return The values as collection of strings.
	 */
	public Collection<String> getValues(List<ValueID> ids)
	{
		ArrayList<String> result = new ArrayList<String>(ids.size());
		for (ValueID id : ids)
			result.add(this.getValue(id));
		
		return result;
	}
	
	/** Returns all monitored values. */
	public Collection<String> getValues()
	{
		return values.values();
	}
	
	/** Stops the monitoring. */
	public void stop() throws IOException
	{
		if (loadavg != null)
			loadavg.close();
		
		if (meminfo != null)
			meminfo.close();
	}
	
	
	/* =============== Helper Methods =============== */
	
	/** Parses and returns the specified value as load-value. */
	public static float parseLoadValue(String value)
	{
		return Float.parseFloat(value);
	}
	
	/** Parses and returns the specified value as memory size in bytes. */
	public static long parseMemoryValue(String value)
	{
		return Long.parseLong(value);
	}
	
	
	/* =============== Internal Methods =============== */
	
	private String readLoadAvgFile() throws IOException
	{
		startIndex = 0;
		loadavg.seek(0);
		return loadavg.readLine();
	}
	
	private String nextLoadField(String data)
	{
		// Find the end index of the field
		int endIndex = data.indexOf(' ', startIndex);
		if (endIndex < 0)
			endIndex = data.length();
		
		String field = data.substring(startIndex, endIndex);
		startIndex = endIndex + 1;  // Advance to next field
		return field;
	}
	
	private String nextMemInfoField(int skipnum) throws IOException
	{
		// Skip specified number of lines
		while (skipnum > 0) {
			meminfo.readLine();
			--skipnum;
		}
		
		final String data = meminfo.readLine();
		
		// Parse the value's unit
		int index = data.lastIndexOf(' ');
		final char unit = data.charAt(index + 1);

		long number = 0;
		char digit;
		
		// Parse the value as 64-bit number
		index = data.lastIndexOf(' ', index - 1);
		while ((digit = data.charAt(++index)) != ' ') {
			// Replace the multiplication by 10 with faster shifts and add operations:
			//     number * 10 = number * (8 + 2) = (number << 3) + (number << 1)
			number = (number << 3L) + (number << 1L) + (long)(digit - '0');
		}
		
		// Convert to bytes
		switch (unit)
		{
			case 'k':
				// Kilobytes
				number *= 1024L;
				break;

			case 'M':
			case 'm':
				// Megabytes
				number *= 1048576L;
				break;
				
			case 'G':
			case 'g':
				// Gigabytes
				number *= 1073741824L;
				break;
		}
		
		return Long.toString(number);
	}
}
