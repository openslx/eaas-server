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

package de.bwl.bwfla.common.services.guacplay.util;


/** A collection of 32-bit flags. */
public final class FlagSet
{
	private int flags;
	
	
	/** Constructor */
	public FlagSet()
	{
		this.flags = 0;
	}
	
	/** Set the specified flag. */
	public void set(int flag)
	{
		flags |= flag;
	}
	
	/** Reset the specified flag. */
	public void reset(int flag)
	{
		flags &= ~flag;
	}
	
	/** Returns true, if the specified flag was set, else false. */
	public boolean enabled(int flag)
	{
		return ((flags & flag) == flag);
	}
	
	/** Reset all flags. */
	public void clear()
	{
		flags = 0;
	}
	
	/** Returns the underlying value. */
	public int value()
	{
		return flags;
	}
}
