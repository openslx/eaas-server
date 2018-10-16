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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;


public class ByteRangeChannel implements ReadableByteChannel
{
	private ReadableByteChannel data;
	private ByteRange range;
	private long remaining;


	public ByteRangeChannel(ReadableByteChannel data, ByteRange range)
	{
		this.setDataChannel(data);
		this.setRange(range);
	}

	public ByteRangeChannel setDataChannel(ReadableByteChannel data)
	{
		if (data == null)
			throw new IllegalArgumentException("Invalid byte channel source!");

		this.data = data;
		return this;
	}

	public ByteRangeChannel setRange(ByteRange range)
	{
		if (range == null)
			throw new IllegalArgumentException("Invalid byte range!");

		this.range = range;
		this.remaining = range.getLength();
		return this;
	}

	public ByteRange getRange()
	{
		return range;
	}

	public long getStartOffset()
	{
		return range.getStartOffset();
	}

	public long getEndOffset()
	{
		return range.getEndOffset();
	}

	public long getLength()
	{
		return range.getLength();
	}

	public long getNumBytesRemaining()
	{
		return remaining;
	}

	public boolean hasBytesRemaining()
	{
		return (remaining > 0);
	}


	/* ========== ReadableByteChannel Implementation ========== */

	@Override
	public int read(ByteBuffer buffer) throws IOException
	{
		// Limit the amount of data to read into buffer
		{
			final long numBytesToRead = Math.min(buffer.remaining(), this.getNumBytesRemaining());
			buffer.limit(buffer.position() + (int) numBytesToRead);
		}

		// Read data from source channel
		final int numBytesRead = data.read(buffer);
		if (numBytesRead > 0)
			this.remaining -= numBytesRead;

		return numBytesRead;
	}

	@Override
	public boolean isOpen()
	{
		return this.hasBytesRemaining() && data.isOpen();
	}

	@Override
	public void close() throws IOException
	{
		// Nothing to do!
	}
}
