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
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.protocol.GuacamoleInstruction;

import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;


/**
 * A custom {@link GuacamoleWriter} for the Guacamole's instruction stream,
 * with the option to register an {@link IGuacInterceptor} instance.
 */
public class GuacWriter implements GuacamoleWriter
{
	private final IGuacInterceptor interceptor;
	private final CharArrayWrapper wrapper;
	private final Writer output;
	private long numBytesWritten;
	private long numMsgsWritten;
	
	
	/** Constructor */
	public GuacWriter(Writer output)
	{
		this(output, null);
	}
	
	/** Constructor */
	public GuacWriter(Writer output, IGuacInterceptor interceptor)
	{
		this.interceptor = interceptor;
		this.wrapper = (interceptor != null) ? new CharArrayWrapper() : null;
		this.output = output;
		this.numBytesWritten = 0L;
		this.numMsgsWritten = 0L;
	}

	/** Returns the number of bytes written by this writer. */
	public long getNumBytesWritten()
	{
		return numBytesWritten;
	}
	
	/** Returns the number of messages written by this writer. */
	public long getNumMsgsWritten()
	{
		return numMsgsWritten;
	}
	
	@Override
	public void write(char[] data, int offset, int length) throws GuacamoleException
	{
		// Handle the data...
		try {
			if (interceptor != null) {
				// Pass it through the interceptor
				wrapper.set(data, offset, length);
				if (interceptor.onClientMessage(wrapper))
					output.write(wrapper.array(), wrapper.offset(), wrapper.length());
			}
			else {
				// Write it directly into the output 
				output.write(data, offset, length);
			}
			
			output.flush();
			
			numBytesWritten += length;
			++numMsgsWritten;
		}
		catch (Exception exception) {
			// Something is broken, rethrow
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else throw new GuacamoleServerException(exception);
		}
	}

	@Override
	public void write(char[] data) throws GuacamoleException
	{
		this.write(data, 0, data.length);
	}

	 @Override
	public void writeInstruction(GuacamoleInstruction instruction) throws GuacamoleException
	{
		this.write(instruction.toString().toCharArray());
	}
}
