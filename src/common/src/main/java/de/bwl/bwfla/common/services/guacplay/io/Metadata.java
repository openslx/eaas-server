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


/** This class represents a set of named chunks of metadata information. */
public final class Metadata
{
	private final LinkedHashMap<String, MetadataChunk> chunks;
	
	
	/** Constructor */
	public Metadata()
	{
		this.chunks = new LinkedHashMap<String, MetadataChunk>();
	}
	
	/** Constructor */
	public Metadata(int capacity)
	{
		this.chunks = new LinkedHashMap<String, MetadataChunk>(capacity);
	}
	
	/**
	 * Adds a new entry to the specified chunk.
	 * @param ctag The chunk's name.
	 * @param key The entry's key.
	 * @param value The entry's value.
	 */
	public void addChunkEntry(String ctag, String key, String value)
	{
		MetadataChunk chunk = chunks.get(ctag);
		if (chunk == null) {
			// Not found, create a new chunk
			chunk = new MetadataChunk(ctag);
			this.addChunk(chunk);
		}
		
		chunk.put(key, value);
	}
	
	/**
	 * Adds a new {@link MetadataChunk}.
	 * @param chunk The chunk to add.
	 */
	public void addChunk(MetadataChunk chunk)
	{
		chunks.put(chunk.getTag(), chunk);
	}
	
	/**
	 * Returns the chunk, corresponding to the specified tag/name.
	 * @param tag The chunk's name to look up.
	 * @return The corresponding chunk, when found, else null.
	 */
	public MetadataChunk getChunk(String tag)
	{
		return chunks.get(tag);
	}
	
	/**
	 * Checks the existence of a chunk with the specified tag/name.
	 * @param tag The chunk's name to look up.
	 * @return true when chunk is available, else false.
	 */
	public boolean containsChunk(String tag)
	{
		return chunks.containsKey(tag);
	}
	
	/** Returns all available chunks. */
	public Collection<MetadataChunk> getChunks()
	{
		return chunks.values();
	}
	
	/** Removes all chunks. */
	public void clear()
	{
		chunks.clear();
	}
	
	/** Returns true, when no chunks are available, else false. */
	public boolean isEmpty()
	{
		return chunks.isEmpty();
	}
}
