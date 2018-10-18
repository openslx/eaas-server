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
import java.util.Iterator;
import java.util.List;


public abstract class ByteRangeIterator implements Iterator<ByteRangeChannel>, AutoCloseable
{
	private final Iterator<ByteRange> ranges;


	protected ByteRangeIterator(List<ByteRange> ranges) throws IOException
	{
		this.ranges = ranges.iterator();
	}

	protected abstract ReadableByteChannel getDataChannel(ByteRange range) throws IOException;


	/* ========== Iterator Implementation ========== */

	@Override
	public boolean hasNext()
	{
		return ranges.hasNext();
	}

	@Override
	public ByteRangeChannel next() throws IllegalStateException, IllegalArgumentException
	{
		try {
			final ByteRange range = ranges.next();
			return new ByteRangeChannel(this.getDataChannel(range), range);
		}
		catch (IOException error) {
			throw new IllegalStateException(error);
		}
	}
}
