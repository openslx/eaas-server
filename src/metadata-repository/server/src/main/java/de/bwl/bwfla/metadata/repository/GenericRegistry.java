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

package de.bwl.bwfla.metadata.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class GenericRegistry<T>
{
	private final Map<String, T> entries;


	public GenericRegistry()
	{
		this.entries = new ConcurrentHashMap<>();
	}


	// ========== Public API ==============================

	public GenericRegistry register(String name, T entry)
	{
		entries.put(name, entry);
		return this;
	}

	public T remove(String name)
	{
		return entries.remove(name);
	}

	public T lookup(String name)
	{
		return entries.get(name);
	}

	public Stream<String> list()
	{
		return entries.keySet().stream();
	}
}
