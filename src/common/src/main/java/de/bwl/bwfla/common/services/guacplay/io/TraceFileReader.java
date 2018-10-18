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
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.services.guacplay.util.CharUtils;


/** A reader for the {@link TraceFile}s. */
public class TraceFileReader extends FileReader
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(TraceFileReader.class);
	
	/* Member fields */
	private final TraceFile file;
	private final BlockIndex index;
	private final CharBuffer buffer;
	private BlockReader curblock;
	
	/** Default size for buffering (8kB) */
	private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
	
	
	/** Package-Private Constructor */
	TraceFileReader(TraceFile file, FileChannel channel) throws IOException
	{
		this(file, channel, DEFAULT_BUFFER_SIZE);
	}
	
	/** Package-Private Constructor */
	TraceFileReader(TraceFile file, FileChannel channel, int bufsize) throws IOException
	{
		super(channel, file.getCharset(), bufsize);
		
		this.file = file;
		this.index = new BlockIndex();
		this.buffer = CharBuffer.allocate(bufsize);
		this.curblock = null;
	}

	/** Prepares the reading-process and loads the metadata. */
	public void prepare() throws IOException
	{
		this.prepare(true);
	}

	/**
	 * Prepares the reading-process and loads the metadata, when specified.
	 * @param loadMetadata If true then the metadata block will be read, else not.
	 */
	public void prepare(boolean loadMetadata) throws IOException,FileReaderException
	{
		this.ensureState(State.READY);
		this.checkFileHeader();
		this.readBlockIndex();
		
		// Reader is now ready for reading operations!
		state = State.READING;
		
		// Load also the metadata, if requested
		if (loadMetadata && this.contains(TraceFileDefs.BLOCKNAME_METADATA))
			this.readMetadataBlock();
	}
	
	/**
	 * Begin reading a new block and setup the specified {@link BlockReader}.
	 * @param block The reader, that will be used for reading the block.
	 */
	public void begin(BlockReader block) throws IOException
	{
		this.ensureState(State.READING);
		
		// Previous block finished?
		if (curblock != null) {
			final String curname = curblock.getBlockName();
			final String message = "Begin reading of a new block '{}', but "
					+ "reading of current block '{}' was not finished properly!";
			log.warn(message, block.getBlockName(), curname);
			log.warn("Finish reading of the current block '{}' now.", curname);
			this.finish();
		}
		
		// Lookup the block
		final String blockname = block.getBlockName();
		final BlockIndexEntry entry = index.lookup(blockname);
		if (entry == null) {
			String message = "The requested block '" + blockname 
					+ "' was not found in file: " + file.getPath();
			throw new FileReaderException(message);
		}
		
		// Prepare this reader
		final int offset = entry.getBlockOffset();
		this.seek(offset);
		this.limit(offset + entry.getBlockLength());
		
		// Prepare block-reader
		block.begin(this, buffer);
		curblock = block;
	}
	
	/** Finish reading of current block. */
	public void finish()
	{
		if (curblock == null) {
			log.warn("No reading operation in progress!");
			return;
		}
		
		curblock.end();
		curblock = null;
	}
	
	/** Returns true, when this file contains the specified block, else false. */
	public boolean contains(String block)
	{
		return index.contains(block);
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */
	
	private void checkFileHeader() throws IOException
	{
		final String header = file.getHeader().toString();
		
		// Read the header from file
		this.seek(0);
		this.limit(header.length());
		this.fill(buffer);
		
		// Check the header's signature
		if (!CharUtils.contains(buffer, header)) {
			String message = "Invalid header found in '" + buffer + "'! Expected was '" + header + "'.";
			throw new FileReaderException(message);
		}
	}
	
	private void readBlockIndex() throws IOException
	{
		int offset = this.size() - 4 * (BlockLocator.MAXLENGTH + TraceFileWriter.EOF_MARKER.length());
		
		// Read last characters from the file
		this.seek(offset);
		this.fill(buffer);
		
		// Find and parse the index-offset 
		final BlockLocator locator = new BlockLocator();
		locator.deserialize(buffer.array(), buffer.position(), buffer.length());
		
		// Read the index-block
		offset = locator.offset();
		this.seek(offset);
		this.limit(offset + locator.length());
		this.fill(buffer);
		
		// Parse the index-block
		IndexBlockReader reader = new IndexBlockReader();
		reader.begin(this, buffer);
		reader.read(index);
		reader.end();
	}
	
	private void readMetadataBlock() throws IOException
	{
		Metadata metadata = file.getMetadata();
		metadata.clear();
		
		// Parse the metadata-block
		MetadataBlockReader block = new MetadataBlockReader();
		this.begin(block);
		block.read(metadata);
		this.finish();
	}
}
