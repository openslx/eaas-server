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

import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.util.CharToken;
import de.bwl.bwfla.common.services.guacplay.util.IntegerToken;
import de.bwl.bwfla.common.services.guacplay.util.LongToken;

import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/** A reader for blocks, containing client-events and server-updates. */
public final class TraceBlockReader extends BlockReader
{
	private final IntegerToken itoken;
	private final LongToken ltoken;
	private final CharToken ctoken;
	
	
	/** Constructor */
	public TraceBlockReader()
	{
		super(TraceBlockWriter.BLOCKNAME);
		this.itoken = new IntegerToken();
		this.ltoken = new LongToken();
		this.ctoken = new CharToken();
	}

	/**
	 * Read a single {@link Message}.
	 * @param message The destination for read message.
	 * @return true, when a valid message was read, else false.
	 */
	public boolean read(Message message) throws IOException
	{
		// Loop, while something can be read
		while (this.hasRemaining()) {
			// Skip indentation
			this.skip(SYMBOL_TAB, SYMBOL_SPACE);
	
			// Is it a comment?
			if (this.peek() == PREFIX_COMMENT) {
				// Yes, skip the whole line and new-line symbol
				this.skipUntil(SYMBOL_NEWLINE);
				this.skip(1);
				
				continue;  // Proceed with next line
			}
	
			// Parse and return the entry
			final long timestamp = this.readTimestamp();
			final char[] data = this.readEventData();
			message.set(SourceType.INTERNAL, timestamp, data, 0, data.length);
			
			return true;  // Valid message was read!
		}
		
		return false;  // No valid message was read!
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */
	
	private long readTimestamp() throws IOException
	{
		if (!this.read(ltoken))
			throw new BlockReaderException("The timestamp could not be parsed!");
		
		// Skip, until next token
		this.skip(DELIMITER_VALUES, SYMBOL_SPACE);
				
		return ltoken.value();
	}
	
	private char[] readEventData() throws IOException
	{
		// Read event's length
		if (!this.read(itoken))
			throw new BlockReaderException("The event's length could not be parsed!");
		
		// Skip, until next token
		this.skip(DELIMITER_VALUES, SYMBOL_SPACE);
		
		// Read the event's data
		final int length = itoken.value();
		if (!this.read(ctoken, length))
			throw new BlockReaderException("The event's data could not be read!");
		
		// Copy the data to a new array, since the original can change!
		final char[] data = new char[length];
		System.arraycopy(ctoken.array(), ctoken.start(), data, 0, length);
		
		// Skip the rest of the line and new-line symbol
		this.skipUntil(SYMBOL_NEWLINE);
		this.skip(1);
		
		return data;
	}
}
