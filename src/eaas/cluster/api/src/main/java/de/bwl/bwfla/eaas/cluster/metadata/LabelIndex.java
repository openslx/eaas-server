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

package de.bwl.bwfla.eaas.cluster.metadata;

import java.util.HashMap;
import java.util.Map;


public class LabelIndex
{
	private final Map<String, String> entries;
	
	public LabelIndex()
	{
		this.entries = new HashMap<String, String>();
	}
	
	public void add(Label label)
	{
		entries.put(label.getKey(), label.getValue());
	}
	
	public void remove(Label label)
	{
		entries.remove(label.getKey());
	}
	
	public boolean contains(Label label)
	{
		return this.contains(label.getKey());
	}
	
	public boolean contains(String key)
	{
		return entries.containsKey(key);
	}
	
	public String get(String key)
	{
		return entries.get(key);
	}
	
	public boolean apply(LabelSelector selector)
	{
		// Try to match the selector, using a special
		// marker-value when label is not found!
		final String key = selector.getKey();
		final String value = entries.getOrDefault(key, Label.UNDEFINED_VALUE);
		return selector.match(value);
	}
}
