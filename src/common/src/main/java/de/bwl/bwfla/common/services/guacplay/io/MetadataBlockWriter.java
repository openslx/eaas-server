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
import java.util.Map.Entry;

import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/** A writer for blocks, containing the {@link Metadata} entries. */
public class MetadataBlockWriter extends BlockWriter
{
	/** Number of indentation for a multi-line value. */
	public static final int MULTILINE_VALUE_INDENTATION = 2 * INDENTATION.length();
	
	
	/** Constructor */
	public MetadataBlockWriter()
	{
		super(BLOCKNAME_METADATA);
	}

	/** Write all specified metadata chunks. */
	public void write(Metadata metadata) throws IOException
	{
		for (MetadataChunk chunk : metadata.getChunks()) {
			// Write the chunk's tag
			buffer.append(INDENTATION);
			buffer.append(YAML_BEGIN_PART);
			buffer.append(SYMBOL_SPACE);
			buffer.append(YAML_TAG_PREFIX);
			buffer.append(chunk.getTag());
			
			// Append the optional comment line
			final String comment = chunk.getComment();
			if ((comment != null) && !comment.isEmpty()) {
				buffer.append(SYMBOL_SPACE);
				buffer.append(SYMBOL_SPACE);
				buffer.append(PREFIX_COMMENT);
				buffer.append(SYMBOL_SPACE);
				buffer.append(comment);
			}
			
			buffer.append(SYMBOL_NEWLINE);
			this.writeChunkEntries(chunk);
		}
	}
	
	/** Write the chunk's key/value pairs. */
	private void writeChunkEntries(MetadataChunk chunk) throws IOException
	{
		// Write the key/value pairs
		for (Entry<String, String> entry : chunk.entrySet()) {
			final String keystr = entry.getKey();
			final String valstr = entry.getValue();
			
			// Write the key part
			buffer.append(INDENTATION);
			buffer.append(keystr);
			buffer.append(YAML_KV_DELIMITER);
			buffer.append(SYMBOL_SPACE);
	
			int pos, from = 0;
			
			// Write the value part, handling new-lines inside it
			if ((pos = valstr.indexOf(SYMBOL_NEWLINE)) > -1) {
				// Value contains new-lines...
				buffer.append(YAML_PRESERVE_NEWLINES);
				buffer.append(SYMBOL_NEWLINE);
				do {
					// Write one line of the string
					for (int i = 0; i < MULTILINE_VALUE_INDENTATION; ++i)
						buffer.append(INDENTATION);
					buffer.append(valstr, from, ++pos);
					from = pos;	 // Update start index and find next \n
					pos = valstr.indexOf(SYMBOL_NEWLINE, from);
				} while (pos > -1);
				
				// Handle the rest of the string...
				for (int i = 0; i < MULTILINE_VALUE_INDENTATION; ++i)
					buffer.append(INDENTATION);
				
				// Append the rest of chars
				if (from < valstr.length())
					buffer.append(valstr, from, valstr.length());
				
				// Else the special case:
				// (from == length) && (last char == \n)
			}
			else {
				// Value does not contain new-lines
				buffer.append(valstr);
			}
			
			buffer.append(SYMBOL_NEWLINE);
			this.flush();
		}
	}
}
