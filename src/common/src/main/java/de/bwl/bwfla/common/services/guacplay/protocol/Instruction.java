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

import de.bwl.bwfla.common.services.guacplay.util.CharArrayBuffer;
import de.bwl.bwfla.common.services.guacplay.util.FlagSet;
import de.bwl.bwfla.common.services.guacplay.util.LongUtils;
import de.bwl.bwfla.common.services.guacplay.util.IntegerUtils;


/** A partially-parsed instruction in Guacamole's protocol format. */
public final class Instruction implements Cloneable
{
	private final FlagSet flags;
	
	// Instruction's data
	private String opcode;
	private char[] data;
	private int offset;
	private int length;
	private int argnum;
	
	// Argument offsets/lengths
	private int[] argoffs;
	private int[] arglens;
	
	// Available Flags
	public static final int FLAG_SHARED_INSTANCE   = 1;
	public static final int FLAG_SHARED_ARRAYDATA  = 1 << 1;
	
	
	/** Constructor */
	public Instruction()
	{
		this(8);
	}
	
	/** Constructor */
	public Instruction(int argcap)
	{
		this.flags = new FlagSet();
		this.opcode = null;
		this.data = null;
		this.offset = 0;
		this.length = 0;
		this.argnum = 0;
		this.argoffs = new int[argcap];
		this.arglens = new int[argcap];
	}
	
	/** Copy-Constructor */
	public Instruction(Instruction other)
	{
		this.flags = new FlagSet();
		this.opcode = other.opcode;
		this.data = new char[other.length];
		this.offset = 0;
		this.length = other.length;
		this.argnum = other.argnum;
		this.argoffs = new int[argnum];
		this.arglens = new int[argnum];
		
		// Copy the data, offsets and lengths
		System.arraycopy(other.data, other.offset, data, 0, length);
		System.arraycopy(other.arglens, 0, arglens, 0, argnum);
		if (other.offset == 0)
			System.arraycopy(other.argoffs, 0, argoffs, 0, argnum);
		else {
			// Update offsets for the new array!
			for (int i = 0; i < argnum; ++i)
				argoffs[i] = other.argoffs[i] - other.offset;
		}
	}
	
	/** Set the instruction's opcode. */
	public void setOpcode(String opcode)
	{
		this.opcode = opcode;
	}

	/** Set the array containing instruction's data. */
	public void setArray(char[] data, int offset, int length)
	{
		this.data = data;
		this.offset = offset;
		this.length = length;
	}
	
	/** Set the array containing instruction's data. */
	public void setArray(char[] data)
	{
		this.data = data;
	}
	
	/** Set the offset of instruction's data. */
	public void setOffset(int offset)
	{
		this.offset = offset;
	}
	
	/** Set the length of instruction's data. */
	public void setLength(int length)
	{
		this.length = length;
	}
	
	/** Returns instruction's flags. */
	public FlagSet flags()
	{
		return flags;
	}
	
	/** Returns the array containing instruction's data. */
	public char[] array()
	{
		return data;
	}
	
	/** Returns the offset of this instruction. */
	public int offset()
	{
		return offset;
	}
	
	/** Returns the length of this instruction. */
	public int length()
	{
		return length;
	}
	
	/** Returns the opcode of this instruction. */
	public String getOpcode()
	{
		return opcode;
	}
	
	/** Returns the number of arguments in this instruction. */
	public int getNumArguments()
	{
		return argnum;
	}
	
	/**
	 * Add a preparsed argument to this instruction.
	 * @param offset The argument's offset.
	 * @param length The argument's length.
	 */
	public void addArgument(int offset, int length)
	{
		// Resize needed?
		if (argnum == this.capacity()) {
			argoffs = this.resizeAndCopy(argoffs);
			arglens = this.resizeAndCopy(arglens);
		}
		
		// Add the values 
		argoffs[argnum] = offset;
		arglens[argnum] = length;
		++argnum;
	}
	
	/** Returns the specified argument as {@link String}. */
	public String argAsString(int index)
	{
		return new String(data, argoffs[index], arglens[index]);
	}
	
	/** Returns the specified argument as {@link CharBuffer}. */
	public CharBuffer argAsCharBuffer(int index)
	{
		return CharBuffer.wrap(data, argoffs[index], arglens[index]);
	}
	
	/** Returns the specified argument as {@link CharArrayBuffer}. */
	public CharArrayBuffer argAsCharArray(int index)
	{
		return new CharArrayBuffer(data, argoffs[index], arglens[index]);
	}
	
	/** Returns the specified argument in the specified {@link CharArrayBuffer}. */
	public void argAsCharArray(int index, CharArrayBuffer outbuf)
	{
		outbuf.set(data, argoffs[index], arglens[index]);
	}
	
	/** Returns the specified argument as an int. */
	public int argAsInt(int index) throws InstructionParserException
	{
		return IntegerUtils.fromBase10(data, argoffs[index], arglens[index]);
	}
	
	/** Returns the specified argument as a long. */
	public long argAsLong(int index)
	{
		return LongUtils.fromBase10(data, argoffs[index], arglens[index]);
	}

	/** Returns the specified argument as a float. */
	public float argAsFloat(int index)
	{
		return Float.parseFloat(this.argAsString(index));
	}
	
	/** Returns the specified argument as a double. */
	public double argAsDouble(int index)
	{
		return Double.parseDouble(this.argAsString(index));
	}
	
	/** Reset the number of arguments. */
	public void clearArguments()
	{
		this.argnum = 0;
	}
	
	/** Full-reset, the instance represents then an empty instruction. */
	public void reset()
	{
		this.opcode = null;
		this.data = null;
		this.offset = 0;
		this.length = 0;
		this.argnum = 0;
	}
	
	@Override
	public Instruction clone()
	{
		return new Instruction(this);
	}
	
	@Override
	public String toString()
	{
		return new String(data, offset, length);
	}
	
	public char[] toCharArray()
	{
		final char[] datacopy = new char[length];
		System.arraycopy(data, offset, datacopy, 0, length);
		return datacopy;
	}
	
	
	/* =============== Internal Methods =============== */
	
	private int capacity()
	{
		return argoffs.length;
	}
	
	private int[] resizeAndCopy(int[] oldbuf)
	{
		int[] newbuf = new int[oldbuf.length << 1];
		System.arraycopy(oldbuf, 0, newbuf, 0, oldbuf.length);
		return newbuf;
	}
}
