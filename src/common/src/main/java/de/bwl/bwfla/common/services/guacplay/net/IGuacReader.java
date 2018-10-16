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

package de.bwl.bwfla.common.services.guacplay.net;

import java.io.Writer;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleReader;


/** An interface for a more efficient {@link GuacamoleReader}. */
public interface IGuacReader extends GuacamoleReader
{
	/** Returns the registered {@link IGuacInterceptor} instance. */
	public IGuacInterceptor getInterceptor();
	
	/**
	 * Reads one complete Guacamole instruction and writes it directly into the specified {@link Writer}.
	 * @param output The destination to write into.
	 * @return true when an instruction was read, else false.
	 */
	public boolean readInto(Writer output) throws GuacamoleException;
}
