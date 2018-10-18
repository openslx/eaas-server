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
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;
import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/** A writer for writing to the {@link TraceFile}. */
public class TraceFileWriter extends FileWriter
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(TraceFileWriter.class);
	
	/* Member fields */
	private final TraceFile file;
	private final StringBuffer strbuf;
	private final BlockIndex index;
	private final BlockLocator locator;
	private BlockWriter curblock;
	
	/** Default size for buffering (8kB) */
	private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	/** Marker, representing the end-of-file. */
	public static final String EOF_MARKER = " ###EOF###";
	
	
	/** Package-Private Constructor */
	TraceFileWriter(TraceFile file, FileChannel channel)
	{
		this(file, channel, DEFAULT_BUFFER_SIZE);
	}
	
	/** Package-Private Constructor */
	TraceFileWriter(TraceFile file, FileChannel channel, int bufsize)
	{
		super(channel, file.getCharset(), bufsize);
		this.file = file;
		this.strbuf = new StringBuffer(128);
		this.index = new BlockIndex();
		this.locator = new BlockLocator();
		this.curblock = null;
	}

	/** Prepare the writing-process. */
	public void prepare() throws IOException
	{
		this.ensureState(State.READY);
		
		TraceFileHeader header = file.getHeader();
		
		// Write the header
		header.serialize(strbuf);
		strbuf.append(SYMBOL_NEWLINE);
		strbuf.append(SYMBOL_NEWLINE);
		this.write(strbuf);
		
		// Writer is now ready for writing operations!
		state = State.WRITING;
	}

	/**
	 * Begin a new block and setup the specified {@link BlockWriter}.
	 * @param block The writer, that will be used for writing the new block.
	 */
	public final void begin(BlockWriter block) throws IOException
	{
		this.ensureState(State.WRITING);

		// Previous block finished?
		if (this.isInsideBlock()) {
			final String curname = curblock.getBlockName();
			String message = "New block '{}' is requested, but current block '{}' was not closed properly!";
			log.warn(message, block.getBlockName(), curname);
			log.warn("Closing current block '{}' now.", curname);
			this.finish();
		}
		
		block.begin(this, strbuf);
		curblock = block;
	}
	
	/** Finish a currently opened block. */
	public final void finish() throws IOException
	{
		if (!this.isInsideBlock()) {
			log.warn("No block to close!");
			return;
		}
		
		this.finishBlock(true);
	}

	/** Returns true, when a block is open, else false. */
	public final boolean isInsideBlock()
	{
		return (curblock != null);
	}
	
	/** Write a new-line into the file. */
	public void newline()
	{
		strbuf.append(SYMBOL_NEWLINE);
	}
	
	/**
	 * Write a single-line comment into the file.
	 * This method is intended for use outside of any blocks!
	 */
	public void comment(String comment) throws IOException
	{
		this.ensureState(State.WRITING);
		
		if (this.isInsideBlock())
			throw new FileWriterException("Use EntryWriter.comment() method for writing comments inside a block!");
		
		// Write comment
		strbuf.append(PREFIX_COMMENT);
		strbuf.append(SYMBOL_SPACE);
		strbuf.append(comment);
		strbuf.append(SYMBOL_NEWLINE);
		
		this.write(strbuf);
	}
	
	@Override
	public void close() throws IOException
	{
		// Writer already closed?
		if (this.isClosed())
			return;
		
		// Append special blocks
		this.writeMetadataBlock();
		this.writeIndexBlock();
		this.writeIndexLocator();

		super.close();
	}

	
	/* ==================== INTERNAL METHODS ==================== */
	
	private void finishBlock(boolean indexed) throws IOException
	{
		curblock.end();
		
		if (indexed) {
			// Add a new entry into the block-index
			final String name = curblock.getBlockName();
			final int offset = curblock.getBlockOffset();
			final int length = curblock.getBlockLength();
			index.add(new BlockIndexEntry(name, offset, length));
		}
		
		curblock = null;
	}
	
	private void writeMetadataBlock() throws IOException
	{
		final Metadata metadata = file.getMetadata();
		
		// Nothing to write?
		if (metadata.isEmpty())
			return;
		
		this.comment("Trace's metadata.");
		
		// Write the metadata block
		MetadataBlockWriter block = new MetadataBlockWriter();
		this.begin(block);
		block.write(metadata);
		this.finishBlock(true);
	}
	
	private void writeIndexBlock() throws IOException
	{
		IndexBlockWriter block = new IndexBlockWriter();
		
		this.comment("Index of all blocks in this file.");
		this.comment("Format: " + block.format());

		// Write the index
		this.begin(block);
		block.write(index);
		this.finishBlock(false);
		
		// Update locator for the index-block
		locator.setOffset(block.getBlockOffset());
		locator.setLength(block.getBlockLength());
	}
	
	private void writeIndexLocator() throws IOException
	{
		locator.serialize(strbuf);
		strbuf.append(EOF_MARKER);
		this.write(strbuf);
	}
}
