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

package de.bwl.bwfla.common.services.guacplay.protocol;

import org.glyptodon.guacamole.protocol.GuacamoleInstruction;
import de.bwl.bwfla.common.services.guacplay.util.IntegerUtils;
import de.bwl.bwfla.common.services.guacplay.util.StringBuffer;
import static de.bwl.bwfla.common.services.guacplay.GuacDefs.*;


/**
 * Helper class for constructing instructions in the Guacamole's protocol
 * format. Objects of this class can be efficiently reused, as opposed to
 * the {@link GuacamoleInstruction} class.
 */
public class InstructionBuilder
{
	private final StringBuffer strbuf;
	private Instruction outinstr;
	
	
	/** Constructor */
	public InstructionBuilder(int capacity)
	{
		this.strbuf = new StringBuffer(capacity);
		this.outinstr = null;
	}
	
	/** Reset the builder state and start construction of a new instruction. */
	public void start(String opcode)
	{
		this.start(opcode, null);
	}
	
	/** Reset the builder state and start construction of a new instruction. */
	public void start(String opcode, Instruction outinstr)
	{
		this.outinstr = outinstr;
		if (outinstr != null) {
			outinstr.setOpcode(opcode);
			outinstr.clearArguments();
		}
		
		// Write opcode
		strbuf.clear();
		strbuf.append(opcode.length());
		strbuf.append(LENGTH_SEPARATOR);
		strbuf.append(opcode);
	}
	
	/** Add a String argument. */
	public void addArgument(String string)
	{
		final int arglen = string.length();
		
		// Write length prefix
		strbuf.append(VALUE_SEPARATOR);
		strbuf.append(arglen);
		strbuf.append(LENGTH_SEPARATOR);
		
		if (outinstr != null)
			outinstr.addArgument(strbuf.length(), arglen);
		
		// Write argument
		strbuf.append(string);
	}
	
	/** Add a char[] argument. */
	public void addArgument(char[] data, int offset, int length)
	{
		// Write length prefix
		strbuf.append(VALUE_SEPARATOR);
		strbuf.append(length);
		strbuf.append(LENGTH_SEPARATOR);
		
		if (outinstr != null)
			outinstr.addArgument(strbuf.length(), length);
		
		// Write argument
		strbuf.append(data, offset, length);
	}
	
	/** Add an int argument. */
	public void addArgument(int number)
	{
		final int arglen = IntegerUtils.getStringLength(number);
		
		// Write length prefix
		strbuf.append(VALUE_SEPARATOR);
		strbuf.append(arglen);
		strbuf.append(LENGTH_SEPARATOR);
		
		if (outinstr != null)
			outinstr.addArgument(strbuf.length(), arglen);
		
		// Write argument
		strbuf.append(number);
	}
	
	/** Finish the construction. */
	public void finish()
	{
		this.finish(true);
	}
	
	/**
	 * Finish the construction of current instruction.
	 * @param shared If true, then the content of the instruction
	 *               will be shared with this builder, else copied.
	 */
	public void finish(boolean shared)
	{
		strbuf.append(INSTRUCTION_TERMINATOR);
		
		if (outinstr == null)
			return;

		final char[] data = strbuf.array();
		final int length = strbuf.length();
		
		// Update the output-instruction
		if (shared) {
			// Don't copy the data-array
			outinstr.setArray(data, 0, length);
		}
		else {
			// Copy the data to a new char-array
			char[] datacopy = strbuf.toCharArray();
			outinstr.setArray(datacopy, 0, datacopy.length);
		}
	}
	
	/** Reset this builder for reusing. */
	public void reset()
	{
		strbuf.clear();
	}
	
	/** Returns the underlying data array. */
	public char[] array()
	{
		return strbuf.array();
	}
	
	/** Returns the length of valid data in the underlying array. */
	public int length()
	{
		return strbuf.length();
	}
	
	/** Returns a copy of the builder's content. */
	public char[] toCharArray()
	{
		return strbuf.toCharArray();
	}
}
