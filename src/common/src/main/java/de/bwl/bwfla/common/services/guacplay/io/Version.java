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


/** Helper class for reading and writing file's version. */
public class Version
{
	private int major;
	private int minor;

	
	/** Constructor */
	public Version()
	{
		this(-1, -1);
	}
	
	/** Constructor */
	public Version(int major, int minor)
	{
		this.major = major;
		this.minor = minor;
	}
	
	/** Returns the major version. */
	public int getMajor()
	{
		return major;
	}

	/** Returns the minor version. */
	public int getMinor()
	{
		return minor;
	}
	
	/** Returns true, when this represents a valid version, else false. */
	public boolean isValid()
	{
		return (major >= 0) && (minor >= 0);
	}
	
	/** Serialize the version and write it to the specified buffer. */
	public void serialize(StringBuffer strbuf)
	{
		strbuf.append(major);
		strbuf.append('.');
		strbuf.append(minor);
	}
	
	@Override
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer(8);
		this.serialize(strbuf);
		return strbuf.toString();
	}
}
