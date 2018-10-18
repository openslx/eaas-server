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

import de.bwl.bwfla.common.services.guacplay.util.IntegerToken;
import de.bwl.bwfla.common.services.guacplay.util.IntegerUtils;
import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;

import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.PREFIX_COMMAND;
import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.DELIMITER_VALUES;


/**
 * This class represents a locator for some
 * specific block in the {@link TraceFile}.
 */
final class BlockLocator
{
	private int offset;
	private int length;

	/** The maximal length of the locator. */
	public static final int MAXLENGTH = 5 + 2 * IntegerUtils.getStringLength(Integer.MAX_VALUE);
	
	/** The minimal length of the locator. */
	public static final int MINLENGTH = 5 + 2;
	
	
	/** Constructor */
	public BlockLocator()
	{
		offset = 0;
		length = 0;
	}
	
	/** Set block's offset. */
	public void setOffset(int offset)
	{
		this.offset = offset;
	}
	
	/** Set block's length. */
	public void setLength(int length)
	{
		this.length = length;
	}
	
	/** Returns block's offset. */
	public int offset()
	{
		return offset;
	}
	
	/** Returns block's length. */
	public int length()
	{
		return length;
	}
	
	/** Returns true, when this locator is valid, else false. */
	public boolean isValid()
	{
		return (offset > 0);
	}
	
	/**
	 * Serialize this locator into the specified output-buffer.
	 * @param output The buffer to use for output.
	 */
	public void serialize(StringBuffer output)
	{
		if (!this.isValid())
			throw new IllegalStateException("The BlockLocator is invalid!");
		
		output.clear();
		output.append(PREFIX_COMMAND);
		output.append(PREFIX_COMMAND);
		output.append(offset);
		output.append(DELIMITER_VALUES);
		output.append(length);
		output.append(PREFIX_COMMAND);
		output.append(PREFIX_COMMAND);
	}
	
	/**
	 * Read and deserialize a locator from the specified input.
	 * @param input The input-array to read from.
	 * @param inpoff The start offset for reading.
	 * @param inplen The maximal number of chars to read.
	 */
	public void deserialize(char[] input, int inpoff, int inplen) throws IOException
	{
		final int maxpos = inpoff + inplen - 1;
		int curpos = inpoff;
		
		// Find the locator's prefix
		while (curpos < maxpos) {
			final char cur = input[curpos];
			final char next = input[++curpos];
			if ((cur == PREFIX_COMMAND) && (cur == next))
				break;  // Prefix found
		}
		
		// Locator found?
		if (curpos == maxpos)
			throw new IOException("BlockLocator could not be found!");
		
		final IntegerToken token = new IntegerToken();
		
		// Reset locator
		offset = 0;
		length = 0;
		
		// Parse the offset
		curpos = BlockLocator.parse(token, input, curpos + 1, maxpos, DELIMITER_VALUES);
		offset = token.value();
		
		// Parse the length
		curpos = BlockLocator.parse(token, input, curpos + 1, maxpos, PREFIX_COMMAND);
		length = token.value();
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */
	
	private static int parse(IntegerToken token, char[] input, int start, int end, char endsym) throws IOException
	{
		int pos = start;
		int number = 0;

		// Parse the integer
		for (; pos < end; ++pos) {
			final char digit = input[pos];
			if (digit == endsym)
				break;  // End reached

			// Update the number with new digit
			number = IntegerUtils.append(number, digit);
		}

		if (pos == start)
			throw new IOException("Locator's token could not be parsed!");

		token.set(number);
		return pos;
	}
}
