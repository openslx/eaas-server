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

package de.bwl.bwfla.common.services.guacplay.io;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


/** An index for {@link TraceFile}'s blocks. */
public class BlockIndex
{
	private final Map<String, BlockIndexEntry> entries;

	
	/** Constructor */
	public BlockIndex()
	{
		this.entries = new LinkedHashMap<String, BlockIndexEntry>();
	}

	/** Add a new entry to the index. */
	public void add(BlockIndexEntry entry)
	{
		entries.put(entry.getBlockName(), entry);
	}
	
	/**
	 * Returns true, when this index contains an entry
	 * for the specified block's name, else false.
	 */
	public boolean contains(String name)
	{
		return entries.containsKey(name);
	}
	
	/** Lookup the entry corresponding to the specified block's name. */
	public BlockIndexEntry lookup(String name)
	{
		return entries.get(name);
	}
	
	/** Remove the entry corresponding to the block's name. */
	public BlockIndexEntry remove(String name)
	{
		return entries.remove(name);
	}
	
	/** Returns all entries in this index. */
	public Collection<BlockIndexEntry> entries()
	{
		return entries.values();
	}
}
