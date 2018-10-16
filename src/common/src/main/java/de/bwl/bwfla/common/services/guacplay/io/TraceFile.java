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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.VERSION_MAJOR;
import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.VERSION_MINOR;


/**
 * A special file containing recorded trace of user-interaction
 * with the emulator and additional metadata information.
 */
public class TraceFile
{
	// Member fields
	private final TraceFileHeader header;
	private final Metadata metadata;
	private final Charset charset;
	private final Path path;
	

	/** Constructor */
	public TraceFile(Path path, Charset charset)
	{
		this(new Version(VERSION_MAJOR, VERSION_MINOR), path, charset);
	}
	
	/** Constructor */
	public TraceFile(Version version, Path path, Charset charset)
	{
		this.header = new TraceFileHeader(version);
		this.metadata = new Metadata();
		this.charset = charset;
		this.path = path;
	}
	
	/** Creates a new trace-file and returns a buffered-writer for writing into it. */
	public TraceFileWriter newBufferedWriter() throws IOException
	{
		// Options for opening the channel
		EnumSet<StandardOpenOption> options = EnumSet.of(StandardOpenOption.CREATE);
		options.add(StandardOpenOption.TRUNCATE_EXISTING);
		options.add(StandardOpenOption.WRITE);
		
		// Open channel and create the writer
		FileChannel channel = (FileChannel) Files.newByteChannel(path, options);
		return new TraceFileWriter(this, channel);
	}
	
	/** Opens a trace-file and returns a buffered-reader for reading from it. */
	public TraceFileReader newBufferedReader() throws IOException
	{
		// Open channel and create the reader
		FileChannel channel = (FileChannel) Files.newByteChannel(path, StandardOpenOption.READ);
		return new TraceFileReader(this, channel);
	}
	
	/** Returns the file's path. */
	public Path getPath()
	{
		return path;
	}
	
	/** Returns the file's charset. */
	public Charset getCharset()
	{
		return charset;
	}
	
	/** Returns the file's version. */
	public Version getVersion()
	{
		return header.getVersion();
	}

	/** Returns the file's metadata entries. */
	public Metadata getMetadata()
	{
		return metadata;
	}
	
	
	/* ==================== For internal usage ==================== */
	
	TraceFileHeader getHeader()
	{
		return header;
	}
}
