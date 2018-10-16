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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/** A mapping from enumeration constants to string values. */
@XmlType(namespace = "http://bwfla.bwl.de/common/datatypes")
public class MonitorValueMap<K extends Enum<K>>
{
	private final ArrayList<String> values;
	
	/** Constant, indicating an invalid monitor-value. */
	public static final String INVALID_VALUE = "";

	
	/** Constructor */
	public MonitorValueMap(int size)
	{
		this.values = new ArrayList<String>(size);
		
		// Preinsert placeholder values
		for (int i = 0; i < size; ++i)
			values.add(INVALID_VALUE);
	}

	/** Sets the new value for specified ID. */
	public void set(K vid, String value)
	{
		int index = vid.ordinal();
		values.set(index, value);
	}
	
	/** Returns the mapped value for specified ID. */
	public String get(K vid)
	{
		int index = vid.ordinal();
		return values.get(index);
	}
	
	/**
	 * Returns true when this map contains a
	 * value for the specified ID, else false.
	 */
	public boolean contains(K vid)
	{
		String value = this.get(vid);
		return MonitorValueMap.valid(value);
	}
	
	/** Returns all values in this map. */
	public List<String> values()
	{
		return values;
	}
	
	/** Removes the mapped value for the specified ID. */
	public void remove(K vid)
	{
		this.set(vid, INVALID_VALUE);
	}
	
	/** Removes all values from this map. */
	public void clear()
	{
		final int size = values.size();
		for (int i = 0; i < size; ++i)
			values.set(i, INVALID_VALUE);
	}

	
	/* ==================== Internal API ==================== */
	
	private MonitorValueMap(List<String> list)
	{
		this.values = new ArrayList<String>(list);
	}
	
	private static boolean valid(String value)
	{
		return !value.contentEquals(INVALID_VALUE);
	}
	
	
	/* ==================== JAXB-Adapter ==================== */
	
	public class Adapter extends XmlAdapter<List<String>, MonitorValueMap<K>>
	{
		@Override
		public List<String> marshal(MonitorValueMap<K> input) throws Exception
		{
			return input.values();
		}

		@Override
		public MonitorValueMap<K> unmarshal(List<String> input) throws Exception
		{
			return new MonitorValueMap<K>(input);
		}
	}
}
