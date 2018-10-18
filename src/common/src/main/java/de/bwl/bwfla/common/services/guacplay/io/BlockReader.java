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

import java.io.IOException;
import java.nio.CharBuffer;

import de.bwl.bwfla.common.services.guacplay.util.CharUtils;
import de.bwl.bwfla.common.services.guacplay.util.CharToken;
import de.bwl.bwfla.common.services.guacplay.util.IntegerToken;
import de.bwl.bwfla.common.services.guacplay.util.IntegerUtils;
import de.bwl.bwfla.common.services.guacplay.util.LongToken;
import de.bwl.bwfla.common.services.guacplay.util.LongUtils;

import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/**
 * A base class for readers, that should read
 * custom blocks from the {@link TraceFile}.
 */
public class BlockReader
{
	/* Member fields */
	private final String name;
	private FileReader input;
	private CharBuffer buffer;

	/** Detail message for an exception, thrown when the token can't fit the buffer. */
	private static final String BUFFER_TOO_SMALL_MESSAGE
			= "Attempt to read a token, that is bigger than the current buffer!";
	
	
	/** Constructor */
	protected BlockReader(String name)
	{
		this.name = name;
		this.reset();
	}
	
	/** Returns the block's name. */
	public final String getBlockName()
	{
		return name;
	}
	
	/** Returns the number of unread characters. */
	public final int remaining() throws IOException
	{
		return (buffer.remaining() + input.remaining());
	}
	
	/** Returns true, when unread characters are available, else false. */
	public final boolean hasRemaining() throws IOException
	{
		return (buffer.hasRemaining() || input.hasRemaining());
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */

	/** Skip the specified number of characters. */
	protected final void skip(int count) throws IOException
	{
		do {
			final int remaining = buffer.remaining();
			
			// Are any chars left in buffer after skipping?
			if (remaining >= count) {
				buffer.position(buffer.position() + count);
				break;  // Yes
			}
			
			// No, refill needed
			count -= remaining;
			
			// Refill the buffer and proceed!
		} while (input.fill(buffer, false));
	}
	
	/** Skip the specified subsequent symbols. */
	protected final void skip(char symbol) throws IOException
	{
		do {
			CharUtils.skipZeroBased(buffer, symbol);
			
			// End of buffer reached?
			if (buffer.hasRemaining())
				break;  // No
			
			// Yes, refill the buffer and proceed!
		} while (input.fill(buffer, false));
	}
	
	/** Skip all of the specified subsequent symbols. */
	protected final void skip(char sym1, char sym2) throws IOException
	{
		do {
			CharUtils.skipZeroBased(buffer, sym1, sym2);
			
			// End of buffer reached?
			if (buffer.hasRemaining())
				break;  // No
			
			// Yes, refill the buffer and proceed!
		} while (input.fill(buffer, false));
	}
	
	/** Skip all of the specified subsequent symbols. */
	protected final void skip(char... symbols) throws IOException
	{
		do {
			CharUtils.skipZeroBased(buffer, symbols);
			
			// End of buffer reached?
			if (buffer.hasRemaining())
				break;  // No
			
			// Yes, refill the buffer and proceed!
		} while (input.fill(buffer, false));
	}

	/** Skip all symbols, until the specified symbol is found. */
	protected final void skipUntil(char symbol) throws IOException
	{
		do {
			final char[] data = buffer.array();
			final int start = buffer.position();
			final int end = buffer.limit();
			final int pos = CharUtils.indexOf(data, start, end, symbol);
			
			// Symbol found?
			if (pos >= 0) {
				buffer.position(pos);
				break;  // Yes
			}
			
			// No, refill the buffer and proceed!
		} while (input.fill(buffer, false));
	}
	
	/** Return the character at reader's current position, without advancing it. */
	protected final char peek() throws IOException
	{
		// At least one char available? 
		if (!buffer.hasRemaining() && !input.fill(buffer))
			throw new BlockReaderException("The end of block was reached!");
		
		return buffer.get(buffer.position());  // Yes
	}
	
	/**
	 * When the specified prefix is found at reader's current position,
	 * true will be returned. The current position will be not advanced! 
	 */
	protected final boolean peek(String prefix) throws IOException
	{
		// Refill needed? 
		if (!buffer.hasRemaining() && !input.fill(buffer))
			throw new BlockReaderException("The end of block was reached!");
		
		final int length = prefix.length();
		
		// Enough chars available? 
		if (buffer.remaining() < length)
			return false;
		
		final char[] array = buffer.array();
		int bufpos = buffer.position();
		int count = 0;
		
		// Compare buffer's content with prefix
		while (count < length) {
			if (array[bufpos] != prefix.charAt(count))
				break;
			
			++bufpos;
			++count;
		}
		
		return (count == length);
	}
	
	/** Read and parse a 32-bit integer in decimal notation. */
	protected final boolean read(IntegerToken token) throws IOException
	{
		boolean valid = false;
		int number = 0;
		int curpos = 0;
		
		do {
			final char[] data = buffer.array();
			final int maxpos = buffer.limit();
			curpos = buffer.position();
			
			// Parse the integer
			for (; curpos < maxpos; ++curpos) {
				final char digit = data[curpos];
				if ((digit < '0') || (digit > '9'))
					break;  // A non-digit character found!
				
				// It's a valid digit!
				number = IntegerUtils.appendUnsafe(number, digit);
				valid = true;
			}
			
			// Whole number parsed?
			if (curpos != maxpos)
				break;  // Yes
			
			// Maybe not, refill the buffer and proceed!
		} while (input.fill(buffer, false));
		
		// Update the state
		buffer.position(curpos);
		token.set(number);
		
		return valid;
	}
	
	/** Read and parse a 64-bit integer in decimal notation. */
	protected final boolean read(LongToken token) throws IOException
	{
		boolean valid = false;
		long number = 0L;
		int curpos = 0;
		
		do {
			final char[] data = buffer.array();
			final int maxpos = buffer.limit();
			curpos = buffer.position();
			
			// Parse the integer
			for (; curpos < maxpos; ++curpos) {
				final char digit = data[curpos];
				if ((digit < '0') || (digit > '9'))
					break;  // A non-digit character found!
				
				// It's a valid digit!
				number = LongUtils.appendUnsafe(number, digit);
				valid = true;
			}
			
			// Whole number parsed?
			if (curpos != maxpos)
				break;  // Yes
			
			// Maybe not, refill the buffer and proceed!
		} while (input.fill(buffer, false));
		
		// Update the state
		buffer.position(curpos);
		token.set(number);
		
		return valid;
	}
	
	/**
	 * Read the next token, until the specified delimiter is found.
	 * @param token The parsed output token.
	 * @param delimiter The delimiter to find.
	 * @return true, when the delimiter was found, else false.
	 */
	protected final boolean read(CharToken token, char delimiter) throws IOException
	{
		boolean refilled = false;
		
		do {
			// Find the token's end
			final char[] data = buffer.array();
			final int start = buffer.position();
			final int end = CharUtils.indexOf(data, start, buffer.limit(), delimiter);
			if (end >= 0) {
				// Delimiter found, update token
				int offset = (refilled) ? 0 : start;
				token.set(data, offset, end);
				buffer.position(end);
				return true;
			}
			
			// Delimiter was not found!
			if (refilled) {
				// We try to refill a second time. The buffer is too small!
				throw new BlockReaderException(BUFFER_TOO_SMALL_MESSAGE);
			}
			
			refilled = true;
			
			// Refill the buffer and retry!
		} while (input.fill(buffer, true));
		
		return false;
	}
	
	/**
	 * Read the next token, until one of the specified delimiters is found.
	 * @param token The parsed output token.
	 * @param delimiters The delimiters to find.
	 * @return true, when one of the delimiters was found, else false.
	 */
	protected final boolean read(CharToken token, char... delimiters) throws IOException
	{
		boolean refilled = false;
		
		do {
			// Find the token's end
			final char[] data = buffer.array();
			final int start = buffer.position();
			final int end = CharUtils.indexOfAny(data, start, buffer.limit(), delimiters);
			if (end >= 0) {
				// Delimiter found, update token
				int offset = (refilled) ? 0 : start;
				token.set(data, offset, end);
				buffer.position(end);
				return true;
			}
			
			// Delimiter was not found!
			if (refilled) {
				// We try to refill a second time. The buffer is too small!
				throw new BlockReaderException(BUFFER_TOO_SMALL_MESSAGE);
			}
			
			refilled = true;
			
			// Refill the buffer and retry!
		} while (input.fill(buffer, true));
		
		return false;
	}
	
	/**
	 * Read the next token with the specified length.
	 * @param token The parsed output token.
	 * @param length The token's length.
	 * @return true, when the token was available, else false.
	 */
	protected final boolean read(CharToken token, int length) throws IOException
	{
		// Can the token be read?
		if (buffer.capacity() < length)
			throw new BlockReaderException(BUFFER_TOO_SMALL_MESSAGE);
		
		// Enough content in the buffer?
		if ((buffer.remaining() < length) && !input.fill(buffer, true))
			return false;  // No
		
		// Yes, then update token
		final int start = buffer.position();
		final int end = start + length;
		token.set(buffer.array(), start, end);
		buffer.position(end);
		
		return true;
	}
	
	/**
	 * Prepare this {@link BlockReader} for reading from a file.
	 * The input is expected to be positioned to the beginning of a block.
	 * @param input The input-reader for the trace-file.
	 * @param buffer The internal buffer for entries.
	 */
	final void begin(FileReader input, CharBuffer buffer) throws IOException   // package-private
	{
		this.input = input;
		this.buffer = buffer;

		final CharToken token = new CharToken();
		this.read(token, SYMBOL_SPACE);
		
		// Check the block-command
		if (!CharUtils.contains(token.array(), token.start(), token.end(), COMMAND_BLOCK_BEGIN))
			throw new BlockReaderException("Invalid command!", COMMAND_BLOCK_BEGIN, token.toString());
		
		// Find next token
		this.skip(SYMBOL_SPACE);
		this.read(token, SYMBOL_NEWLINE, SYMBOL_SPACE);
		
		// Check the block name
		if (!CharUtils.contains(token.array(), token.start(), token.end(), name))
			throw new BlockReaderException("Invalid block-name!", name, token.toString());
		
		// Find the end of line
		this.skipUntil(SYMBOL_NEWLINE);
		this.skip(1);
	}
	
	/** Finish reading this block. */
	final void end()
	{
		this.reset();
	}
	
	/** Reset this reader, making it invalid.  */
	private final void reset()
	{
		this.input = null;
		this.buffer = null;
	}
}
