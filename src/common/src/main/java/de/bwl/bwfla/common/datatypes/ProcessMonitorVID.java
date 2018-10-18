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

package de.bwl.bwfla.common.datatypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/** IDs of the monitored process-values */
@XmlType(namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlEnum(Integer.class)
public enum ProcessMonitorVID
{
	@XmlEnumValue("0")  STATE,                /* State of the process. */
	@XmlEnumValue("1")  USER_MODE_TIME,       /* Amount of time the process was scheduled in user-mode. */
	@XmlEnumValue("2")  KERNEL_MODE_TIME,     /* Amount of time the process was scheduled in kernel-mode. */
	@XmlEnumValue("3")  VIRTUAL_MEMORY_SIZE,  /* Virtual memory size in bytes. */
	@XmlEnumValue("4")  INSTRUCTION_POINTER;  /* The current EIP (instruction pointer). */
	
	
	/** Returns the raw ID of enumeration constant. */
	public int value()
	{
		return this.ordinal();
	}

	/** Converts from raw ID to corresponding enumeration constant. */
	public static ProcessMonitorVID fromValue(int value)
	{
		if (value < 0 || value >= CACHED_VALUES.length) {
			String message = "Attempt to create a ProcessMonitorVID from invalid value '" + value + "'!";
			throw new IllegalArgumentException(message); 
		}
		
		return CACHED_VALUES[value];
	}

	/** Returns the number of enumeration constants. */
	public static int getNumConstants()
	{
		return CACHED_VALUES.length;
	}
	
	// Cache the values once, since Enum.values() always returns a copy of the internal array!
	private static final ProcessMonitorVID[] CACHED_VALUES = ProcessMonitorVID.values();
}
