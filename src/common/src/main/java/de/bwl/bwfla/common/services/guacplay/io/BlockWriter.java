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

import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;
import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/**
 * A base class for writers, that should write
 * custom blocks into the {@link TraceFile}.
 */
public class BlockWriter
{
	/* Member fields */
	private final String name;
	private int offset;
	private int length;
	private FileWriter output;
	protected StringBuffer buffer;
	
	
	/** Constructor */
	protected BlockWriter(String name)
	{
		this.name = name;
		this.reset();
	}
	
	/** Flushes the buffer to the underlying {@link FileWriter}. */
	protected final void flush() throws IOException
	{
		output.write(buffer);
	}
	
	/**
	 * Write a single-line comment in default format. Subclasses
	 * can override this method for writing custom comments.
	 */
	public void comment(String comment) throws IOException
	{
		// Write comment
		buffer.append(INDENTATION);
		buffer.append(PREFIX_COMMENT);
		buffer.append(SYMBOL_SPACE);
		buffer.append(comment);
		buffer.append(SYMBOL_NEWLINE);
		
		this.flush();
	}
	
	/** Returns the block's name. */
	public final String getBlockName()
	{
		return name;
	}
	
	/** Returns the block's offset in the output-file. */
	public final int getBlockOffset()
	{
		return offset;
	}
	
	/** Returns the block's length. */
	public final int getBlockLength()
	{
		return length;
	}

	
	/* ==================== INTERNAL METHODS ==================== */

	/** Reset this writer, making it invalid.  */
	private final void reset()
	{
		this.output = null;
		this.buffer = null;
	}
	
	/**
	 * Prepare this {@link BlockWriter} for writing into a file.
	 * @param output The output-writer.
	 * @param buffer The internal buffer for entries.
	 */
	void begin(FileWriter output, StringBuffer buffer) throws IOException   // package-private
	{
		this.output = output;
		this.buffer = buffer;
		
		// Save the block's offset
		offset = output.position();
		
		// Write the block definition command
		buffer.append(COMMAND_BLOCK_BEGIN);
		buffer.append(SYMBOL_SPACE);
		buffer.append(name);
		buffer.append(SYMBOL_NEWLINE);
		
		this.flush();
	}
	
	/** Finish writing this block. */
	final void end() throws IOException   // package-private
	{
		// Update the block's length
		length = output.position() - offset;
		
		// Write block end
		buffer.append(COMMAND_BLOCK_END);
		buffer.append(SYMBOL_SPACE);
		buffer.append(PREFIX_COMMENT);
		buffer.append(SYMBOL_SPACE);
		buffer.append(name);
		buffer.append(SYMBOL_NEWLINE);
		buffer.append(SYMBOL_NEWLINE);
		
		this.flush();
		this.reset();
	}
}
