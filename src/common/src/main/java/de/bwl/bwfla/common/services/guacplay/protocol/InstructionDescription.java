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

package de.bwl.bwfla.common.services.guacplay.protocol;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;


/** A helper class, containing instruction-specific data. */
public class InstructionDescription
{
	private long timestamp;
	private SourceType source;

	
	/** Constructor */
	public InstructionDescription()
	{
		this(SourceType.UNKNOWN);
	}
	
	/** Constructor */
	public InstructionDescription(SourceType source)
	{
		this.timestamp = 0L;
		this.source = source;
	}
	
	/** Returns the instruction's timestamp. */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	/** Set a new instruction's timestamp. */
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	/** Returns the instruction's origin type. */
	public SourceType getSourceType()
	{
		return source;
	}
	
	/** Set instruction's origin type. */
	public void setSourceType(SourceType source)
	{
		this.source = source;
	}
}
