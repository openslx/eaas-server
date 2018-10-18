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

package de.bwl.bwfla.common.logging;

import java.util.ArrayList;
import java.util.List;


public class PrefixLoggerContext
{
	private final List<Entry> entries;
	private String prefix;
	
	public PrefixLoggerContext()
	{
		this.entries = new ArrayList<Entry>();
		this.prefix = null;
	}
	
	public PrefixLoggerContext(PrefixLoggerContext other)
	{
		this.entries = new ArrayList<Entry>(other.entries);
		this.prefix = null;
	}

	public PrefixLoggerContext add(String value)
	{
		entries.add(new Entry(value));
		prefix = null;
		return this;
	}
	
	public PrefixLoggerContext add(String key, String value)
	{
		entries.add(new Entry(key, value));
		prefix = null;
		return this;
	}
	
	public void update()
	{
		prefix = this.toString();
	}
	
	public String prefix()
	{
		if (prefix == null)
			this.update();
		
		return prefix;
	}
	
	@Override
	public String toString()
	{
		final String beginToken = "(";
		final String endToken = ") ";
		final String kvSeparator = ":";
		final String entrySeparator = "|";
		
		if (entries.isEmpty())
			return "";
		
		final StringBuilder sb = new StringBuilder(512)
				.append(beginToken);
		
		// Serialize the entries...
		for (Entry entry : entries) {
			if (entry.hasKey()) {
				sb.append(entry.key())
				  .append(kvSeparator);
			}
			
			sb.append(entry.value())
			  .append(entrySeparator);
		}
		
		sb.setLength(sb.length() - entrySeparator.length());
		sb.append(endToken);
		
		return sb.toString();
	}
	
	public int size()
	{
		return entries.size();
	}
	
	
	private static class Entry
	{
		private static final String DUMMY_KEY = "__missing";
		
		private final String key;
		private final String value;
		
		public Entry(String value)
		{
			this(DUMMY_KEY, value);
		}
		
		public Entry(String key, String value)
		{
			if (key == null || key.isEmpty())
				throw new IllegalArgumentException("Key is missing or empty!");
			
			if (value == null || value.isEmpty())
				throw new IllegalArgumentException("Value is missing or empty!");
			
			this.key = key;
			this.value = value;
		}
		
		public boolean hasKey()
		{
			return !key.contentEquals(DUMMY_KEY);
		}
		
		public String key()
		{
			return key;
		}
		
		public String value()
		{
			return value;
		}
	}
}
