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
import java.util.List;
import de.bwl.bwfla.common.datatypes.MonitorValueMap;
import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;


/** A helper class for monitoring process-resources. */
public class ProcessMonitor
{
	private final RandomAccessFile statfile;
	private final MonitorValueMap<ProcessMonitorVID> values;
	private int startIndex;
	
	/** Constant, indicating an invalid monitor-value. */
	public static final String INVALID_VALUE = MonitorValueMap.INVALID_VALUE;
	
	/** Constant, indicating an invalid list of monitor-values. */
	public static final List<String> INVALID_VALUE_LIST = new ArrayList<String>(0);
	
	/* Possible states of the monitored process */
	public static final String STATE_RUNNING     = "R";
	public static final String STATE_SLEEPING    = "S";
	public static final String STATE_DISK_SLEEP  = "D";
	public static final String STATE_STOPPED     = "T";
	public static final String STATE_DEAD        = "X";
	
	
	/** Constructor */
	public ProcessMonitor(int pid) throws FileNotFoundException
	{
		this.statfile = new RandomAccessFile("/proc/" + pid + "/stat", "r");
		this.values = new MonitorValueMap<ProcessMonitorVID>(ProcessMonitorVID.getNumConstants());
	}
	
	/**
	 * Update the monitored values.
	 * @return true when successful, else false.
	 */
	public boolean update()
	{
		String stats;
		
		// Read all status fields from OS
		try {
			statfile.seek(0);
			stats = statfile.readLine();
		}
		catch (IOException e) {
			// Likely the monitored process was already terminated, making
			// the PID invalid and with it the stat-file unavailable!
			
			values.clear();
			return false;
		}
		
		startIndex = 0;
		
		// Update status fields...
		values.set(ProcessMonitorVID.STATE, this.readStatField(stats, 2));  // (3) state
		values.set(ProcessMonitorVID.USER_MODE_TIME, this.readStatField(stats, 10));  // (14) utime
		values.set(ProcessMonitorVID.KERNEL_MODE_TIME, this.readStatField(stats, 0));  // (15) stime
		values.set(ProcessMonitorVID.VIRTUAL_MEMORY_SIZE, this.readStatField(stats, 7));  // (23) vsize
		values.set(ProcessMonitorVID.INSTRUCTION_POINTER, this.readStatField(stats, 6));  // (30) kstkeip
		
		return true;
	}
	
	/**
	 * Returns a single monitored value.
	 * @param id The ID of the value to return.
	 * @return The value as string. If the value is not valid, then an empty string is returned.
	 */
	public String getValue(ProcessMonitorVID id)
	{
		return values.get(id);
	}
	
	/**
	 * Returns specific monitored values.
	 * @param ids The IDs of the values to return.
	 * @return The values as collection of strings.
	 */
	public List<String> getValues(Collection<ProcessMonitorVID> ids)
	{
		ArrayList<String> result = new ArrayList<String>(ids.size());
		for (ProcessMonitorVID id : ids)
			result.add(this.getValue(id));
		
		return result;
	}
	
	/** Returns all monitored values. */
	public List<String> getValues()
	{
		return values.values();
	}
	
	/** Stops the monitoring. */
	public void stop() throws IOException
	{
		if (statfile != null)
			statfile.close();
		
		values.clear();
	}
	
	
	/* ==================== Helper Methods ==================== */
	
	/** Parses and returns the specified value as time amount. */
	public long parseTimeAmountValue(String value)
	{
		return Long.parseLong(value);
	}
	
	/** Parses and returns the specified value as memory size in bytes. */
	public static long parseMemoryValue(String value)
	{
		return Long.parseLong(value);
	}
	
	/** Parses and returns the specified value as memory pointer. */
	public static long parseInstrPointerValue(String value)
	{
		return Long.parseLong(value);
	}
	
	
	/* ==================== Internal Methods ==================== */
	
	private String readStatField(String stats, int skipnum)
	{
		// Skipt the specified number of fields
		for (int i = 0; i < skipnum; ++i)
			startIndex = stats.indexOf(' ', startIndex) + 1;
		
		// Find the end index of the field
		int endIndex = stats.indexOf(' ', startIndex);
		if (endIndex < 0)
			endIndex = stats.length();
		
		String field = stats.substring(startIndex, endIndex);
		startIndex = endIndex + 1;  // Advance to next field
		return field;
	}
}
