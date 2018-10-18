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


public class ByteRange
{
	/** Max. range length supported by current implementation */
	public static final int MAX_SUPPORTED_RANGE_LENGTH = 512 * 1024 * 1024;

	private long offset;
	private long length;


	public ByteRange(long offset, long length)
	{
		if (offset < 0)
			ByteRange.fail("Invalid start offset!", offset, length);

		if (length < 1 || length > MAX_SUPPORTED_RANGE_LENGTH)
			ByteRange.fail("Invalid length!", offset, length);

		this.offset = offset;
		this.length = length;
	}

	public long getStartOffset()
	{
		return offset;
	}

	public long getEndOffset()
	{
		return (offset + length - 1L);
	}

	public long getLength()
	{
		return length;
	}

	private static void fail(String message, long offset, long length)
	{
		message += " Range arguments: offset=" + offset + ", length=" + length;
		throw new IllegalArgumentException(message);
	}
}
