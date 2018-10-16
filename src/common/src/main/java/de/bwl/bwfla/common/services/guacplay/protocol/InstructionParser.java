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

import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayBuffer;
import de.bwl.bwfla.common.services.guacplay.util.IntegerUtils;


/** Parser for instructions in Guacamole's protocol format. */
public class InstructionParser
{
	private final CharArrayBuffer buffer;
	private String opcode;
	private int ioffset;
	private int ilength;
	
	
	/** Constructor */
	public InstructionParser()
	{
		this.buffer = new CharArrayBuffer();
		this.opcode = null;
		this.ioffset = 0;
		this.ilength = 0;
	}

	/**
	 * Set new input data to parse.
	 * @param array The array containing data.
	 * @param offset The offset, where valid data begins.
	 * @param length The length of valid data.
	 */
	public void setInput(char[] array, int offset, int length)
	{
		buffer.set(array, offset, length);
	}
	
	/** Returns the underlying data-array. */
	public char[] getDataArray()
	{
		return buffer.array();
	}
	
	/** Returns the offset of valid data in the underlying data-array. */
	public int getDataOffset()
	{
		return buffer.offset();
	}
	
	/** Returns the length of valid data in the underlying data-array. */
	public int getDataLength()
	{
		return buffer.length();
	}
	
	/** Returns the parser's current position in the inderlying data-array. */
	public int getCurrentPosition()
	{
		return buffer.position();
	}
	
	/**
	 * Returns the offset of current instruction. <p/>
	 * NOTE: Returned value is valid only after {@link #parseOpcode()} is called!
	 */
	public int getInstrOffset()
	{
		return ioffset;
	}
	
	/**
	 * Returns the length of current instruction. <p/>
	 * NOTE: Returned value is valid only after {@link #parseArguments(Instruction)} is called!
	 */
	public int getInstrLength()
	{
		 return ilength;
	}
	
	/** Returns true, when tokens are available, else false. */
	public boolean available()
	{
		return (buffer.remaining() > 0);
	}
	
	/**
	 * Parse the whole instruction and save it into the given {@link Instruction} object.
	 * @param outinstr The destination object for parsed values.
	 * @see #parseOpcode(Instruction)
	 * @see #parseArguments(Instruction)
	 */
	public String parse(Instruction outinstr)
	{
		this.parseOpcode();
		this.parseArguments(outinstr);
		return opcode;
	}
	
	/**
	 * Parse only the opcode of the next instruction.
	 * @return The parsed opcode.
	 * @see #parse(Instruction)
	 * @see #parseArguments(Instruction)
	 */
	public String parseOpcode()
	{
		// Save start-offset of this instruction
		ioffset = buffer.position();
		ilength = 0;
		
		// Parse the opcode's length prefix and construct it
		final int length = InstructionParser.parseLength(buffer);
		opcode = new String(buffer.array(), buffer.position(), length);
		
		// Skip the parsed chars
		buffer.skip(length);
		return opcode;
	}
	
	/**
	 * Parse all arguments of the current instruction.
	 * @param outinstr The destination object for parsed arguments.
	 * @return The number of parsed arguments.
	 * @see #parse(Instruction)
	 * @see #parseOpcode(Instruction)
	 */
	public int parseArguments(Instruction outinstr)
	{
		outinstr.setOpcode(opcode);
		outinstr.clearArguments();
		
		int arglen = 0;
		
		// Process all arguments and add them to outinstr
		while (buffer.get() != GuacDefs.INSTRUCTION_TERMINATOR) {
			// Preparse current argument
			arglen = InstructionParser.parseLength(buffer);
			outinstr.addArgument(buffer.position(), arglen);
			
			// Skip the parsed chars
			buffer.skip(arglen);
		}
		
		// Update instruction's length and output
		ilength = buffer.position() - ioffset;
		outinstr.setArray(buffer.array(), ioffset, ilength);
		return outinstr.getNumArguments();
	}
	
	/** Skip all arguments of the current instruction. */
	public void skipArguments()
	{
		while (buffer.get() != GuacDefs.INSTRUCTION_TERMINATOR) {
			// Parse the tokens's length prefix and skip it
			buffer.skip(InstructionParser.parseLength(buffer));
		}
		
		// Update the length of this instruction
		ilength = buffer.position() - ioffset;
	}
	
	/** Skip the specified number of chars of the current instruction. */
	public void skip(int n)
	{
		buffer.skip(n);
	}
	
	/** Parse and return the length prefix at current position. */
	private static int parseLength(CharArrayBuffer srcbuf)
	{
		int number = 0;
		char character;
		
		// Parse number from the buffer
		while ((character = srcbuf.get()) != GuacDefs.LENGTH_SEPARATOR)
			number = IntegerUtils.append(number, character);
		
		return number;
	}
}
