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

import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;
import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/** A helper class for reading and writing {@link TraceFile}'s header. */
final class TraceFileHeader
{
	private final Version version;
	
	
	/** Constructor */
	public TraceFileHeader()
	{
		this(new Version());
	}
	
	/** Consrtuctor */
	public TraceFileHeader(Version version)
	{
		this.version = version;
	}
	
	/** Returns the header's version. */
	public Version getVersion()
	{
		return version;
	}
	
	/** Serialize this header into the specified buffer. */
	public void serialize(StringBuffer strbuf)
	{
		if (!version.isValid())
			throw new IllegalStateException("File version is not specified!");
		
		strbuf.clear();
		
		// Write signature + version string
		strbuf.append(COMMAND_SIGNATURE);
		strbuf.append(SYMBOL_SPACE);
		strbuf.append('v');
		version.serialize(strbuf);
	}
	
	@Override
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer(64);
		this.serialize(strbuf);
		return strbuf.toString();
	}
}
