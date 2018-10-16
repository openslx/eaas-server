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
import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;
import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/** A reader for blocks, containing the {@link Metadata}. */
public class MetadataBlockReader extends BlockReader
{
	private final CharToken ctoken;
	private final StringBuffer valbuf;
	
	/** Constructor */
	public MetadataBlockReader()
	{
		super(BLOCKNAME_METADATA);
		this.ctoken = new CharToken();
		this.valbuf = new StringBuffer(1024);
	}

	/** Read all chunks into the specified object. */
	public void read(Metadata metadata) throws IOException
	{
		MetadataChunk chunk;
		
		while ((chunk = this.read()) != null)
			metadata.addChunk(chunk);
	}
	
	/** Read a metadata chunk. */
	public MetadataChunk read() throws IOException
	{
		MetadataChunk chunk = this.readChunkHeader();
		if (chunk == null)
			return null;  // No chunk found!
		
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
			
			// Begin of a new chunk?
			else if (this.peek(YAML_BEGIN_PART))
				break;  // Yes
			
			// Parse the key/value-entry
			String key = this.readKey();
			String value = this.readValue();
			chunk.put(key, value);
		}
		
		return chunk;
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */
	
	private MetadataChunk readChunkHeader() throws IOException
	{
		// Try to read the chunk header
		while (this.hasRemaining()) {
			// Skip indentation
			this.skip(SYMBOL_TAB, SYMBOL_SPACE);

			// Is it a comment?
			if (this.peek() == PREFIX_COMMENT) {
				// Yes, skip the whole line and new-line symbol
				this.skipUntil(SYMBOL_NEWLINE);
				this.skip(1);
			}

			// Begin of a chunk?
			else if (this.peek(YAML_BEGIN_PART)) {
				// Skip until start of tag-prefix
				this.skipUntil(YAML_TAG_PREFIX);
				this.skip(YAML_TAG_PREFIX);
				
				// Yes, then parse its header
				String tag = this.readTag();
				String comment = this.readComment();
				return new MetadataChunk(tag, comment);
			}

			// Proceed with next line
		}
		
		return null;
	}
	
	private String readTag() throws IOException
	{
		if (!this.read(ctoken, SYMBOL_NEWLINE, SYMBOL_SPACE, PREFIX_COMMENT))
			throw new BlockReaderException("A metadata-chunk's tag could not be parsed!");
	
		// Skip, until comment
		this.skip(SYMBOL_SPACE);
		
		return ctoken.toString();
	}
	
	private String readComment() throws IOException
	{
		if (this.peek() != PREFIX_COMMENT) {
			this.skip(1);  // Skip new-line symbol
			return null;   // No comment available!
		}
	
		// Read comment only, skipping prefix
		this.skip(PREFIX_COMMENT, SYMBOL_SPACE);
		if (!this.read(ctoken, SYMBOL_NEWLINE))
			throw new BlockReaderException("A metadata-chunk's comment could not be parsed!");

		this.skip(1);  // Skip new-line symbol
		
		return ctoken.toString();
	}
	
	private String readKey() throws IOException
	{
		if (!this.read(ctoken, YAML_KV_DELIMITER, SYMBOL_SPACE))
			throw new BlockReaderException("A metadata key could not be parsed!");
	
		// Skip, until value token
		this.skip(YAML_KV_DELIMITER, SYMBOL_SPACE);
		
		return ctoken.toString();
	}
	
	private String readValue() throws IOException
	{
		// A multi-line string to parse?
		if (this.peek() != YAML_PRESERVE_NEWLINES) {
			// No, read a single line value
			if (!this.read(ctoken, SYMBOL_NEWLINE, PREFIX_COMMENT))
				throw new BlockReaderException("A metadata value could not be parsed!");
		
			// Skip, until next line
			this.skipUntil(SYMBOL_NEWLINE);
			this.skip(1);
			
			return ctoken.toString();
		}
	
		// It is a multi-line string!
		
		// Skip to the next line
		this.skipUntil(SYMBOL_NEWLINE);
		this.skip(1);
		
		// Read the first line
		this.readIndentedLine(ctoken, MetadataBlockWriter.MULTILINE_VALUE_INDENTATION);
		valbuf.clear();
		valbuf.append(ctoken.array(), ctoken.start(), ctoken.length());
		
		// Process next lines...
		while (this.hasRemaining()) {
			// Read the next line
			if (!this.readIndentedLine(ctoken, MetadataBlockWriter.MULTILINE_VALUE_INDENTATION))
				break;  // Next key/value entry reached!
			
			// Add it to current content
			valbuf.append(SYMBOL_NEWLINE);
			valbuf.append(ctoken.array(), ctoken.start(), ctoken.length());
		}
		
		return valbuf.toString();
	}
	
	private boolean readIndentedLine(CharToken token, int indentnum) throws BlockReaderException, IOException
	{
		int counter = 0;

		// Skip specified number of indentation symbols
		while ((counter < indentnum) && this.hasRemaining()) {
			if (!this.peek(TraceFileDefs.INDENTATION))
				break;
			
			this.skip(TraceFileDefs.INDENTATION.length());
			++counter;
		}
		
		if (counter < indentnum)
			return false;  // Not indented line
		
		if (!this.read(token, SYMBOL_NEWLINE))
			throw new BlockReaderException("One line of a multi-line value could not be parsed!");
		
		this.skip(1);  // New-line symbol
		return true;
	}
}
