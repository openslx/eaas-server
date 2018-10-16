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

import java.nio.CharBuffer;

import org.junit.Assert;
import org.junit.Test;

import de.bwl.bwfla.common.services.guacplay.BaseTest;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionParser;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionParserException;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayBuffer;


public class InstructionParserTest extends BaseTest
{
	private static final String[][] INSTRUCTION_DATA = {
			{ "select", "vnc" },
			{ "connect", "localhost", "5900", "", "", "true", "" },
			{ "size", "0", "720", "400" },
			{ "name", "LibVNCServer" },
			{ "mouse", "705", "270", "0"},
			{ "key" , "65507", "1" },
			{ "sync", "1384534160529" },
			{ "png", "12", "-1", "0", "0", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAAZ0lEQVQ4jZ3TWwoAIQhA0avM/rfs/NTQZA9VCMryJEEA1kYpFMDMqCLaJ1VEx0UF0TmRRRyQRZZABtkCUeQIRJBntyEizgJc8tfBokim4eIDWrF0JPWIvfh2+NTBWJzqQqs3RyL0zV/oTi0OpxGdVgAAAABJRU5ErkJggg==" }
	};
	
	
	@Test
	public void testArgumentParsing() throws InstructionParserException
	{	
		log.info("Testing instruction's argument-parsing...");
		
		final StringBuilder builder = new StringBuilder(512);
		final InstructionParser parser = new InstructionParser();
		final Instruction instruction = new Instruction(4);
		
		for (String[] parts : INSTRUCTION_DATA) {
			builder.setLength(0);
			
			// Construct the instruction
			for (String part : parts) {
				builder.append(part.length());
				builder.append('.');
				builder.append(part);
				builder.append(',');
			}
			
			builder.setCharAt(builder.length() - 1, ';');
			
			final String instr = builder.toString();
			System.out.println("Parsing: " + instr);
			{
				char[] chars = instr.toCharArray();
				parser.setInput(chars, 0, chars.length);
			}
			
			// Parse opcode
			final String opcode = parser.parseOpcode();
			if (!parts[0].contentEquals(opcode))
				Assert.fail("Parsing opcode failed: " + opcode + " != " + parts[0]);
			
			// Preparse all arguments
			final int argnum = parser.parseArguments(instruction);
			if (argnum != (parts.length - 1))
				Assert.fail("Parsing arguments failed: " + argnum + " != " + (parts.length - 1));
			
			// Parse and check all arguments
			for (int index = 0; index < argnum; ++index) {
				final String arg = parts[index + 1];
				if (arg.isEmpty() || Character.isAlphabetic(arg.charAt(0))) {
					// Parse it as String
					final String str = instruction.argAsString(index);
					if (!str.contentEquals(arg))
						Assert.fail("Parsing a string failed: " + str + " != " + arg);
					
					// Parse it as CharBuffer
					final CharBuffer buf = instruction.argAsCharBuffer(index);
					if (!str.contentEquals(buf))
						Assert.fail("Parsing a CharBuffer failed: " + buf + " != " + arg);
					
					// Parse it as CharArrayWrapper
					final CharArrayBuffer wrapper = instruction.argAsCharArray(index);
					final String wrapperString = new String(wrapper.array(), wrapper.position(), wrapper.length());
					if (!str.contentEquals(wrapperString))
						Assert.fail("Parsing a CharArrayWrapper failed: " + wrapperString + " != " + arg);
				}
				else {
					// It is an integer
					final long exp = Long.parseLong(arg);
					final boolean isint = (exp >= (long)Integer.MIN_VALUE && exp <= (long)Integer.MAX_VALUE);
					final long cur = (isint) ? (long) instruction.argAsInt(index) : instruction.argAsLong(index);
					if (cur != exp)
						Assert.fail("Parsing an integer failed: " + cur + " != " + exp);
				}
			}
		}
		
		this.markAsPassed();
	}
}
