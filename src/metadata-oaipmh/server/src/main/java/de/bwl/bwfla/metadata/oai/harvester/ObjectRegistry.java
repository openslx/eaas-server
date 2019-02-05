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

package de.bwl.bwfla.metadata.oai.harvester;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ObjectRegistry<T>
{
	private final Map<String, T> entries = new ConcurrentHashMap<>();

	public boolean put(String name, T entry)
	{
		return entries.put(name, entry) == null;
	}

	public T lookup(String name)
	{
		return entries.get(name);
	}

	public boolean remove(String name)
	{
		return entries.remove(name) != null;
	}

	public Collection<String> list()
	{
		return entries.keySet();
	}

	public Collection<T> values()
	{
		return entries.values();
	}
}
