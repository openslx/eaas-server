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

import java.io.Reader;
import java.io.Writer;

import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.ReaderGuacamoleReader;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;

import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;


/**
 * A custom {@link GuacamoleReader} for the Guacamole's instruction stream,
 * with the option to register an {@link IGuacInterceptor} instance.
 */
public class GuacReader extends ReaderGuacamoleReader implements IGuacReader
{
	private static final char[] EMPTY_ARRAY = new char[0];
	
	private final IGuacInterceptor interceptor;
	private final CharArrayWrapper wrapper;
	private long numBytesRead;
	private long numMsgsRead;
	
	
	/** Constructor */
	public GuacReader(Reader input)
	{
		this(input, null);
	}
	
	/** Constructor */
	public GuacReader(Reader input, IGuacInterceptor interceptor)
	{
		super(input);
		this.interceptor = interceptor;
		this.wrapper = (interceptor != null) ? new CharArrayWrapper() : null;
		this.numBytesRead = 0L;
		this.numMsgsRead = 0L;
	}
	
	/** Returns the number of messages read by this reader. */
	public long getNumMsgsRead()
	{
		return numMsgsRead;
	}

	/** Returns the number of bytes read by this reader. */
	public long getNumBytesRead()
	{
		return numBytesRead;
	}
	
	@Override
	public IGuacInterceptor getInterceptor()
	{
		return interceptor;
	}

	@Override
	public boolean readInto(Writer output) throws GuacamoleException
	{
		try {
			// Read the data or block
			final char[] data = super.read();
			if (data == null) {
				// End-of-stream reached!
				return false;
			}
			
			// Handle the data...
			if (interceptor != null) {
				// Pass it through the interceptor
				wrapper.set(data, 0, data.length);
				if (interceptor.onServerMessage(wrapper))
					output.write(wrapper.array(), wrapper.offset(), wrapper.length());
			}
			else {
				// Write it directly into the output 
				output.write(data, 0, data.length);
			}
			
			numBytesRead += data.length;
			++numMsgsRead;
		}
		catch (Exception exception) {
			// Something is broken, rethrow
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else throw new GuacamoleServerException(exception);
		}
		
		return true;  // Reading was successful!
	}

	@Override
	public char[] read() throws GuacamoleException
	{
		try {
			// Read the data or block
			char[] data = super.read();
			if (data == null)
				return null;

			numBytesRead += data.length;
			++numMsgsRead;

			// Pass the data through the interceptor
			if (interceptor != null) {
				wrapper.set(data, 0, data.length);
				if (interceptor.onServerMessage(wrapper))
					data = wrapper.toCharArray();
				else data = EMPTY_ARRAY;
			}
			
			return data;
		}
		catch (Exception exception) {
			// Something is broken, rethrow
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else throw new GuacamoleServerException(exception);
		}
	}
}
