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

import java.util.LinkedHashMap;


/**
 * This class represents a named chunk of metadata information,
 * that can contain multiple key/value entries of type string.
 */
@SuppressWarnings("serial")
public final class MetadataChunk extends LinkedHashMap<String, String>
{
	private final String tag;
	private String comment;
	
	
	/** Constructor */
	public MetadataChunk(String tag)
	{
		this(tag, null);
	}
	
	/** Constructor */
	public MetadataChunk(String tag, String comment)
	{
		super();
		
		if (tag.isEmpty())
			throw new IllegalArgumentException("A non-empty tag must be specified!");
		
		this.tag = tag;
		this.comment = comment;
	}
	
	/** Constructor */
	public MetadataChunk(String tag, int capacity)
	{
		this(tag, null, capacity);
	}
	
	/** Constructor */
	public MetadataChunk(String tag, String comment, int capacity)
	{
		super(capacity);
		
		if (tag.isEmpty())
			throw new IllegalArgumentException("A non-empty tag must be specified!");
		
		this.tag = tag;
		this.comment = comment;
	}
	
	/**
	 * Lookup and parse the value as integer.
	 * @param key The entry's name to lookup.
	 * @return The value as 32-bit integer.
	 */
	public int getAsInt(String key)
	{
		String value = this.get(key);
		return Integer.parseInt(value);
	}
	
	/**
	 * Lookup and parse the value as integer.
	 * @param key The entry's name to lookup.
	 * @return The value as 64-bit integer.
	 */
	public long getAsLong(String key)
	{
		String value = this.get(key);
		return Long.parseLong(value);
	}
	
	/**
	 * Lookup and parse the value as floating point number.
	 * @param key The entry's name to lookup.
	 * @return The value as 32-bit floating point number.
	 */
	public float getAsFloat(String key)
	{
		String value = this.get(key);
		return Float.parseFloat(value);
	}
	
	/**
	 * Lookup and parse the value as floating point number.
	 * @param key The entry's name to lookup.
	 * @return The value as 64-bit floating point number.
	 */
	public double getAsDouble(String key)
	{
		String value = this.get(key);
		return Double.parseDouble(value);
	}
	
	/**
	 * Lookup and parse the value as boolean.
	 * @param key The entry's name to lookup.
	 * @return The value as boolean.
	 */
	public boolean getAsBoolean(String key)
	{
		String value = this.get(key);
		return Boolean.parseBoolean(value);
	}
	
	/**
	 * Specify the chunk's single-line comment/description.
	 * @param comment A short single-line comment.
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	
	/** Returns the chunk's comment/description. */
	public String getComment()
	{
		return comment;
	}
	
	/** Returns the chunk's tag/name. */
	public String getTag()
	{
		return tag;
	}
}
