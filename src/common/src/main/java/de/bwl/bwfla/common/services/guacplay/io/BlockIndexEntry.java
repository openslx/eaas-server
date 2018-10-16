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


/** This class represents an entry in the {@link BlockIndex}. */
public class BlockIndexEntry
{
	private String name;
	private int offset;
	private int length;
	
	
	/** Constructor */
	public BlockIndexEntry(String name, int offset)
	{
		this(name, offset, -1);
	}
	
	/** Constructor */
	public BlockIndexEntry(String name, int offset, int length)
	{
		this.name = name;
		this.offset = offset;
		this.length = length;
	}
	
	/** Returns the block's name. */
	public String getBlockName()
	{
		return name;
	}
	
	/** Returns the block's offset. */
	public int getBlockOffset()
	{
		return offset;
	}
	
	/** Returns the block's length. */
	public int getBlockLength()
	{
		return length;
	}
	
	/** Set the block's length. */
	public void setBlockLength(int length)
	{
		this.length = length;
	}
}