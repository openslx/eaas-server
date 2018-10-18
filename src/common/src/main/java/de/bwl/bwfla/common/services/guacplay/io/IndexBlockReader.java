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

import de.bwl.bwfla.common.services.guacplay.util.CharToken;
import de.bwl.bwfla.common.services.guacplay.util.IntegerToken;
import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/** A reader for blocks, containing the {@link BlockIndex}. */
public final class IndexBlockReader extends BlockReader
{
	private final CharToken ctoken;
	private final IntegerToken itoken;
	
	
	/** Constructor */
	public IndexBlockReader()
	{
		super(BLOCKNAME_INDEX);
		this.ctoken = new CharToken();
		this.itoken = new IntegerToken();
	}
	
	/** Read all entries into the specified index. */
	public void read(BlockIndex index) throws IOException
	{
		BlockIndexEntry entry;
		
		while ((entry = this.read()) != null)
			index.add(entry);
	}
	
	/** Read an index-entry. */
	public BlockIndexEntry read() throws IOException
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
	
			// Parse the index-entry
			final String name = this.readBlockName();
			final int offset  = this.readBlockOffset();
			final int length  = this.readBlockLength();
			return new BlockIndexEntry(name, offset, length);
		}
		
		return null;
	}

	
	/* ==================== INTERNAL METHODS ==================== */
	
	private String readBlockName() throws IOException
	{
		if (!this.read(ctoken, DELIMITER_VALUES, SYMBOL_SPACE))
			throw new BlockReaderException("The block-name could not be parsed!");
	
		// Skip, until next token
		this.skip(DELIMITER_VALUES, SYMBOL_SPACE);
		
		return ctoken.toString();
	}
	
	private int readBlockOffset() throws IOException
	{
		if (!this.read(itoken))
			throw new BlockReaderException("The block-offset could not be parsed!");
		
		// Skip, until next token
		this.skip(DELIMITER_VALUES, SYMBOL_SPACE);
		
		return itoken.value();
	}
	
	private int readBlockLength() throws IOException
	{
		if (!this.read(itoken))
			throw new BlockReaderException("The block-length could not be parsed!");
		
		// Skip the whole line and new-line symbol
		this.skipUntil(SYMBOL_NEWLINE);
		this.skip(1);

		return itoken.value();
	}
}
