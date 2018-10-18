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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;


/** A base class for buffered file-writers. */
public class FileWriter implements Closeable, Flushable
{
	private final FileChannel channel;
	private final CharsetEncoder encoder;
	private final ByteBuffer outbuf;
	private int numBytesFlushed;
	protected State state;
	
	/** Writer's states. */
	public static enum State
	{
		READY,
		WRITING,
		CLOSED
	}

	
	/** Constructor */
	protected FileWriter(FileChannel channel, Charset charset, int bufsize)
	{
		this.channel = channel;
		this.encoder = charset.newEncoder();
		this.outbuf = ByteBuffer.allocate(bufsize);
		this.numBytesFlushed = 0;
		this.state = State.READY;
	}
	
	/** Returns the number of bytes written, using this writer. */
	public final int getNumBytesWritten()
	{
		return (numBytesFlushed + outbuf.position());
	}
	
	/** Returns the number of bytes flushed to disk. */
	public final int getNumBytesFlushed()
	{
		return numBytesFlushed;
	}
	
	/** Returns the writer's state. */
	public final State getState()
	{
		return state;
	}
	
	/** Returns the current position in the output-file. */
	public final int position() throws IOException
	{
		// offset := number of bytes written + buffered
		return ((int) channel.position() + outbuf.position());
	}
	
	@Override
	public void flush() throws IOException
	{
		// Nothing to write?
		if (outbuf.position() == 0)
			return;
		
		// Write the buffer to channel
		outbuf.flip();
		numBytesFlushed += channel.write(outbuf);
		outbuf.clear();
	}
	
	@Override
	public void close() throws IOException
	{
		// Flush the internal encoder state
		CoderResult result;
		do {
			result = encoder.flush(outbuf);
			this.flush();
		} while (result != CoderResult.UNDERFLOW);
		
		// Close the writer and channel
		state = State.CLOSED;
		channel.close();
	}

	/** Returns true, when the writer was closed, else false. */
	public final boolean isClosed()
	{
		return (state == State.CLOSED);
	}

	/** Writes the specified buffer and resets it for reuse. */
	final void write(StringBuffer data) throws IOException   // package-private
	{
		final CharBuffer inpbuf = data.buffer();
		
		// Encode everything into the specified charset
		while (inpbuf.hasRemaining()) {
			final CoderResult result = encoder.encode(inpbuf, outbuf, true);
			if (result.isOverflow()) {
				// Output-buffer is full, flush it to disk
				this.flush();
			}
			else if (result.isError()) {
				// Something gone wrong!
				String message = "An error occured during encoding: " + result.toString();
				throw new FileWriterException(message);
			}
			
			// Continue encoding
		}
		
		data.clear();
	}
	
	
	/* ==================== Internal Methods ==================== */
	
	protected final void ensureState(State reqState)
	{
		if (state != reqState) {
			String message = "The Writer is in an invalid state! Expected: " 
					+ reqState.toString() + ", Current: " + state.toString();
			throw new IllegalStateException(message);
		}
	}
}
