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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;


/** A base class for buffered file-readers. */
public class FileReader implements Closeable
{
	/* Member fields */
	private final FileChannel channel;
	private final CharsetDecoder decoder;
	private final ByteBuffer inpbuf;
	private int numRemainingBytes;
	private int readLimit;
	protected State state; 
	
	/** Reader's states. */
	public static enum State
	{
		READY,
		READING,
		CLOSED
	}
	
	
	/** Constructor */
	protected FileReader(FileChannel channel, Charset charset, int bufsize) throws IOException
	{
		this.channel = channel;
		this.decoder = charset.newDecoder();
		this.inpbuf = FileReader.newByteBuffer(decoder, bufsize);
		this.state = State.READY;
		
		this.reset();
	}

	/** Returns the size of the trace-file. */
	public final int size() throws IOException
	{
		return (int) channel.size();
	}
	
	/** Returns the current position in the input-file. */
	public final int position() throws IOException
	{
		return (int) channel.position();
	}
	
	/** Returns the number of unread bytes. */
	public final int remaining() throws IOException
	{
		return numRemainingBytes;
	}
	
	/** Returns true, when unread bytes are available, else false. */
	public final boolean hasRemaining() throws IOException
	{
		return (numRemainingBytes > 0);
	}
	
	@Override
	public void close() throws IOException
	{
		// Close the underlying channel
		channel.close();
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */
	
	protected final void ensureState(State reqState)
	{
		if (state != reqState) {
			String message = "The reader is in an invalid state! Expected: " 
					+ reqState.toString() + ", Current: " + state.toString();
			throw new IllegalStateException(message);
		}
	}
	
	/** Move the reader to the specified file-position. */
	protected final void seek(int newpos) throws IOException
	{
		channel.position(newpos);
		this.reset();
	}
	
	/** Set the limit-offset for reading operations. */
	protected final void limit(int limit) throws IOException
	{
		if ((limit < 0) || (limit > this.size())) {
			String message = "Attempt to set the read-limit to " + limit 
					+ ", but a valid range is [0," + this.size() + "]!";
			throw new IllegalArgumentException(message);
		}
		
		readLimit = limit;
	}
	
	/**
	 * Fill the specified buffer with new data, discarding current content.
	 * @param outbuf The output-buffer.
	 * @return true, when buffer was filled with new data, else false.
	 * @see FileReader#fill(CharBuffer, boolean)
	 */
	protected final boolean fill(CharBuffer outbuf) throws IOException
	{
		return this.fill(outbuf, false);
	}
	
	/**
	 * Fill the specified buffer with new data.
	 * @param outbuf The output-buffer.
	 * @param refill If true, then the unread data in outbuf will be preserved and copied to the beginning.
	 *               If false, then the current content in outbuf will be discarded.
	 * @return true, when buffer was filled with new data, else false. 
	 */
	protected final boolean fill(CharBuffer outbuf, boolean refill) throws IOException
	{
		// Handle output-buffer's content
		if (refill && outbuf.hasRemaining())
			outbuf.compact();  // Copy remaining chars to the beginning.
		else outbuf.clear();   // Discard whole content!

		boolean endOfInput = false;
		boolean doflush = false;
		
		// Read bytes from file and decode them into chars...
		do {
			endOfInput = !this.hasRemaining();
			
			// Try to decode bytes into chars
			final CoderResult result = decoder.decode(inpbuf, outbuf, endOfInput);
			if (result.isOverflow()) {
				break;  // Output buffer is full!
			}
			else if (result.isUnderflow()) {
				// End-of-stream reached?
				if (doflush) {
					decoder.flush(outbuf);
					break;  // Yes
				}
				
				// No, more data from file needed!
				// Prepare input-buffer for writing...
				if (inpbuf.hasRemaining())
					inpbuf.compact();  // Keep the remaining data
				else inpbuf.clear();   // No remaining data to keep

				// Update the write-limit
				final int unread = this.getNumUnreadBytes();
				if (inpbuf.remaining() > unread)
					inpbuf.limit(inpbuf.position() + unread);

				// Read more bytes into buffer
				doflush = channel.read(inpbuf) < 1;
				inpbuf.flip();  // Prepare for reading
				
				this.updateNumRemainingBytes();
			}
			else if (result.isError()) {
				// Something gone wrong!
				String message = "An error occured during decoding: " + result.toString();
				throw new FileReaderException(message);
			}
		} while (!endOfInput);
		
		this.updateNumRemainingBytes();
		
		outbuf.flip();  // Prepare for reading
		return outbuf.hasRemaining();
	}

	/** Reset the internal state. */
	private final void reset() throws IOException
	{
		decoder.reset();
		inpbuf.limit(0);
		readLimit = this.size();
		
		this.updateNumRemainingBytes();
	}
	
	/** Computes the current number of undecoded bytes. */
	private final void updateNumRemainingBytes() throws IOException
	{
		numRemainingBytes = inpbuf.remaining() + this.getNumUnreadBytes();
	}
	
	/** Computes the current number of unread bytes. */
	private final int getNumUnreadBytes() throws IOException
	{
		return (readLimit - this.position());
	}
	
	/** Allocate a new buffer of specified size. */
	private static ByteBuffer newByteBuffer(CharsetDecoder decoder, int sizeInChars)
	{
		float avgsize = ((float) sizeInChars) / decoder.averageCharsPerByte();
		return ByteBuffer.allocate((int) avgsize);
	}
}
