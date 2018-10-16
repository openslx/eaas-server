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

package de.bwl.bwfla.common.utils;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class FileRangeIterator extends ByteRangeIterator
{
	private final SeekableByteChannel channel;

	public FileRangeIterator(Path file, List<ByteRange> ranges) throws IOException
	{
		super(ranges);
		this.channel = Files.newByteChannel(file);
	}

	@Override
	protected ReadableByteChannel getDataChannel(ByteRange range) throws IOException
	{
		channel.position(range.getStartOffset());
		return channel;
	}


	/* ========== AutoCloseable Implementation ========== */

	@Override
	public void close() throws IOException
	{
		channel.close();
	}
}
