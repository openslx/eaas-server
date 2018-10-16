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
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.protocol.GuacamoleInstruction;

import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;
import de.bwl.bwfla.common.services.guacplay.util.ConditionVariable;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;
import de.bwl.bwfla.common.services.guacplay.util.RingBufferSPSC;


/** A custom socket for the {@link PlayerTunnel}. */
public final class PlayerSocket implements GuacamoleSocket
{
	// Member fields
	private final GuacReaderWrapper reader;
	private final GuacWriter writer;
	private GuacamoleWriter curwriter;
	private boolean opened;
	
	/** A dummy-writer instance.  */
	private static final DisabledWriter DISABLED_WRITER = new DisabledWriter();
	
	/** Constructor */
	public PlayerSocket(GuacReader reader, GuacWriter writer, ICharArrayConsumer output, int msgBufferCapacity)
	{
		this.reader = new GuacReaderWrapper(reader, output, msgBufferCapacity);
		this.writer = writer;
		this.curwriter = DISABLED_WRITER;
		this.opened = true;
	}

	/** Post a new message for the socket's reader. */
	public void post(char[] data, int offset, int length)
	{
		reader.post(data, offset, length);
	}
	
	/** Disable writing through this socket. */
	public void disableWriting()
	{
		curwriter = DISABLED_WRITER;
	}
	
	/** Enable writing through this socket. */
	public void enableWriting()
	{
		curwriter = writer;
	}
	
	
	/* =============== GuacamoleSocket Implementation =============== */
	
	@Override
	public GuacamoleWriter getWriter()
	{
		return curwriter;
	}
	
	@Override
	public GuacamoleReader getReader()
	{
		return reader;
	}

	@Override
	public boolean isOpen()
	{
		return opened;
	}

	@Override
	public void close() throws GuacamoleException
	{
		opened = false;
	}
}


/** Internal class for reading from a message queue and a wrapped reader. */
final class GuacReaderWrapper implements IGuacReader
{
	// Member fields
	private final RingBufferSPSC<CharArrayWrapper> messages;
	private final ConditionVariable condition;
	private final ICharArrayConsumer output;
	private final GuacReader reader;
	
	/** A timeout for waiting on empty/full queue. */
	private static final long RETRY_TIMEOUT = 500L;
	
	
	/** Constructor */
	public GuacReaderWrapper(GuacReader reader, ICharArrayConsumer output, int msgBufferCapacity)
	{
		final CharArrayWrapper[] entries = new CharArrayWrapper[msgBufferCapacity];
		for (int i = 0; i < msgBufferCapacity; ++i)
			entries[i] = new CharArrayWrapper();
		
		this.messages = new RingBufferSPSC<CharArrayWrapper>(entries);
		this.condition = new ConditionVariable();
		this.output = output;
		this.reader = reader;
	}

	/** Add a new message to the queue. */
	public void post(char[] data, int offset, int length)
	{
		CharArrayWrapper message = null;
		
		// Retry until the message can be added!
		while ((message = messages.beginPutOp()) == null)
			condition.await(RETRY_TIMEOUT);
		
		message.set(data, offset, length);
		messages.finishPutOp();
	}

	
	/* =============== IGuacReader Implementation =============== */
	
	@Override
	public boolean available() throws GuacamoleException
	{
		return reader.available() || !messages.isEmpty();
	}

	@Override
	public GuacamoleInstruction readInstruction() throws GuacamoleException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IGuacInterceptor getInterceptor()
	{
		return reader.getInterceptor();
	}

	@Override
	public boolean readInto(Writer writer) throws GuacamoleException
	{
		boolean isDataRead = false;
		
		try {
			// Write data from posted messages
			CharArrayWrapper message = null;
			while ((message = messages.beginTakeOp()) != null) {
				try {
					char[] data = message.array();
					int offset = message.offset();
					int length = message.length();
					writer.write(data, offset, length);
					isDataRead = true;
				}
				finally {
					message.reset();
					messages.finishTakeOp();
				}
			}
			
			// Write data from tunnel
			final char[] data = this.read();
			if (data != null) {
				writer.write(data, 0, data.length);
				isDataRead = true;
			}
		}
		catch (Exception exception) {
			throw new GuacamoleServerException(exception);
		}
		
		return isDataRead;
	}
	
	@Override
	public char[] read() throws GuacamoleException
	{
		final char[] data = reader.read();
		if (data != null) {
			try {
				output.consume(data, 0, data.length);
			}
			catch (Exception exception) {
				throw new GuacamoleServerException(exception);
			}
		}
		
		return data;
	}
}


/** Special writer, that does nothing. */
final class DisabledWriter implements GuacamoleWriter
{
	@Override
	public void write(char[] data) throws GuacamoleException
	{
		// Do nothing!
	}

	@Override
	public void write(char[] data, int offset, int length) throws GuacamoleException
	{
		// Do nothing!
	}

	@Override
	public void writeInstruction(GuacamoleInstruction instruction) throws GuacamoleException
	{
		// Do nothing!
	}
}
