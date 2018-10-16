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


/** A writer for blocks, containing the {@link BlockIndex}. */
public final class IndexBlockWriter extends BlockWriter
{
	/** Constructor */
	public IndexBlockWriter()
	{
		super(BLOCKNAME_INDEX);
	}
	
	/* Write all entries in the specified index. */
	public void write(BlockIndex index) throws IOException
	{
		for (BlockIndexEntry entry : index.entries())
			this.write(entry);
	}

	/** Write an index-entry. */
	public void write(BlockIndexEntry entry) throws IOException
	{
		buffer.append(INDENTATION);
		buffer.append(entry.getBlockName());
		buffer.append(DELIMITER_VALUES);
		buffer.append(entry.getBlockOffset());
		buffer.append(DELIMITER_VALUES);
		buffer.append(entry.getBlockLength());
		buffer.append(SYMBOL_NEWLINE);
		
		this.flush();
	}
	
	/** Returns the format description of the entries. */
	public String format()
	{
		StringBuffer strbuf = new StringBuffer(32);
		strbuf.append("<name>");
		strbuf.append(DELIMITER_VALUES);
		strbuf.append("<offset>");
		strbuf.append(DELIMITER_VALUES);
		strbuf.append("<length>");
		return strbuf.toString();
	}
}
