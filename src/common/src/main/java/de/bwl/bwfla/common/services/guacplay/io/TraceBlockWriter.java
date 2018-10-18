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

import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;

import static de.bwl.bwfla.common.services.guacplay.io.TraceFileDefs.*;


/** A writer for blocks, containing client-events and server-updates. */
public final class TraceBlockWriter extends BlockWriter
{
	private int numEntriesWritten;
	
	/** The name of the trace-block. */
	public static final String BLOCKNAME = "trace";
	
	
	/** Constructor */
	public TraceBlockWriter()
	{
		super(BLOCKNAME);
		
		this.numEntriesWritten = 0;
	}

	/** Write an event/update with a timestamp. */
	public void write(InstructionDescription desc, Instruction instr) throws IOException
	{
		this.write(desc.getTimestamp(), instr.array(), instr.offset(), instr.length());
	}
	
	/** Write an event/update with a timestamp. */
	public void write(long timestamp, char[] data, int offset, int length) throws IOException
	{
		buffer.append(INDENTATION);
		buffer.append(timestamp);
		buffer.append(DELIMITER_VALUES);
		buffer.append(length);
		buffer.append(DELIMITER_VALUES);
		buffer.append(data, offset, length);
		buffer.append(SYMBOL_NEWLINE);
		
		this.flush();
		
		++numEntriesWritten;
	}
	
	/** Returns the format description of the entries. */
	public String format()
	{
		StringBuffer strbuf = new StringBuffer(32);
		strbuf.append("<timestamp>");
		strbuf.append(DELIMITER_VALUES);
		strbuf.append("<event's length>");
		strbuf.append(DELIMITER_VALUES);
		strbuf.append("<event>");
		return strbuf.toString();
	}
	
	/** Returns the number of entries, that were written to current block. */
	public int getNumEntriesWritten()
	{
		return numEntriesWritten;
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */
	
	@Override
	void begin(FileWriter output, StringBuffer buffer) throws IOException   // package-private
	{
		super.begin(output, buffer);
		this.numEntriesWritten = 0;
	}
}
