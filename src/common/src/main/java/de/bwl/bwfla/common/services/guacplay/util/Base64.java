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

package de.bwl.bwfla.common.services.guacplay.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;


/** Helper class for encoding/decoding into/from the Base64-format. */
public final class Base64
{
	/* Mask constants */
	private static final int MASK_6BIT  = 0x3F;
	private static final int MASK_8BIT  = 0xFF;
	private static final int MASK_16BIT = 0xFFFF;
	private static final int MASK_24BIT = 0xFFFFFF;
	
	/* Shift constants for 6-bit operations */
	private static final int SHIFT_1x6BIT = 6;
	private static final int SHIFT_2x6BIT = 2 * SHIFT_1x6BIT;
	private static final int SHIFT_3x6BIT = 3 * SHIFT_1x6BIT;
	
	/* Shift constants for 8-bit operations */
	private static final int SHIFT_1x8BIT = 8;
	private static final int SHIFT_2x8BIT = 2 * SHIFT_1x8BIT;
	private static final int SHIFT_3x8BIT = 3 * SHIFT_1x8BIT;
	private static final int SHIFT_4x8BIT = 4 * SHIFT_1x8BIT;
	
	/** Character used for padding. */
	private static final char PAD_CHAR = '=';
	
	/** Mapping of 6-bit patterns to the Base64 alphabet. */
	private static final char[] CTABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	
	/** Mapping of the Base64 alphabet to the 6-bit patterns. */
	private static final int[] ITABLE = new int[256];
	static {
		Arrays.fill(ITABLE, -1);
		ITABLE[PAD_CHAR] = 0;
		for (int i = 0, end = CTABLE.length; i < end; ++i)
			ITABLE[CTABLE[i]] = i;
	}
	
	
	/** Returns the decoded length in bytes of the Base64-encoded {@link CharBuffer}. */
	public static int getDecodedLength(CharBuffer input) throws IOException
	{
		final int inpoff = input.arrayOffset() + input.position();
		final int inplen = input.length();
		return Base64.getDecodedLength(input.array(), inpoff, inplen);
	}
	
	/** Returns the decoded length in bytes of the Base64-encoded char-array. */
	public static int getDecodedLength(char[] input, int inpoff, int inplen) throws IOException
	{
		int padnum = Base64.getNumPadCharacters(input, inpoff, inplen);
		return Base64.getDecodedLength(inplen, padnum);
	}
	
	/** Returns the Base64-encoded length in chars of the {@link ByteBuffer}. */
	public static int getEncodedLength(ByteBuffer input)
	{
		final int inplen = input.remaining();
		return Base64.getEncodedLength(inplen);
	}
	
	/** Returns the Base64-encoded length in chars of the input buffer. */
	public static int getEncodedLength(int inplen)
	{
		return (((inplen + 2) / 3) << 2);
	}
	
	/**
	 * Encodes the inpbuf as Base64 and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param inpbuf The bytes to encode.
	 * @param outbuf The Base64-encoded result.
	 * @return outbuf or a newly allocated {@link CharBuffer}
	 */
	public static CharBuffer encode(ByteBuffer inpbuf, CharBuffer outbuf)
	{
		final int inpoff = inpbuf.arrayOffset() + inpbuf.position();
		final int inplen = inpbuf.remaining();
		return Base64.encode(inpbuf.array(), inpoff, inplen, outbuf);
	}
	
	/**
	 * Encodes the input as Base64 and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param input The input buffer to encode.
	 * @param inpoff The offset, where the data inside the input begins.
	 * @param inplen The length of the data to encode.
	 * @param outbuf The Base64-encoded result.
	 * @return outbuf or a newly allocated {@link CharBuffer}
	 */
	public static CharBuffer encode(byte[] input, int inpoff, int inplen, CharBuffer outbuf)
	{
		final int width = 3;
		final int remainder = inplen % width;
		final int outlen = Base64.getEncodedLength(inplen);
		if ((outbuf == null) || (outbuf.capacity() < outlen)) {
			// Output buffer is too small, resize
			outbuf = CharBuffer.allocate(outlen);
		}
		else {
			// Output buffer is big enough
			outbuf.clear();
		}
		
		// Counters for input/output processing. (-1 needed for prefix-increments!)
		final char[] output = outbuf.array();
		int outpos = outbuf.arrayOffset() + outbuf.position() - 1;
		final int inend = inpoff + inplen - remainder - 1;
		int inpos = inpoff - 1;
		
		int b2, b1, bits;
		
		// Encode all byte triplets into Base64-characters
		while (inpos < inend) {
			// Assemble the bit pattern from three bytes
			b2 = (input[++inpos] & MASK_8BIT) << SHIFT_2x8BIT;
			b1 = (input[++inpos] & MASK_8BIT) << SHIFT_1x8BIT;
			bits = b2 | b1 | (input[++inpos] & MASK_8BIT);
			
			// Construct the encoded chars from bit pattern
			outpos = Base64.encodeAs4x6Bit(output, outpos, bits);
		}
		
		// Encode the last byte triplet, handling the padding
		if (remainder > 0) {
			bits = 0;
			
			// Assemble the bit pattern from last bytes
			for (int i = remainder, shift = SHIFT_2x8BIT; i > 0; --i, shift -= SHIFT_1x8BIT)
				bits |= (input[++inpos] & MASK_8BIT) << shift;
			
			// Construct the chars from bit pattern
			for (int i = remainder + 1, shift = SHIFT_3x6BIT; i > 0; --i, shift -= SHIFT_1x6BIT)
				output[++outpos] = CTABLE[(bits >>> shift) & MASK_6BIT];
			
			// Add padding chars
			final int padnum = width - remainder;
			for (int i = 0; i < padnum; ++i)
				output[++outpos] = PAD_CHAR;
		}
		
		outbuf.limit(outlen);
		return outbuf;
	}
	
	/**
	 * Encodes the inpbuf as Base64 and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param inpbuf The integers to encode.
	 * @param outbuf The Base64-encoded result.
	 * @return outbuf or a newly allocated {@link CharBuffer}
	 */
	public static CharBuffer encode(IntBuffer inpbuf, CharBuffer outbuf)
	{
		final int inpoff = inpbuf.arrayOffset() + inpbuf.position();
		final int inplen = inpbuf.remaining();
		return Base64.encode(inpbuf.array(), inpoff, inplen, outbuf);
	}
	
	/**
	 * Encodes the input as Base64 and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param input The input buffer to encode.
	 * @param inpoff The offset, where the data inside the input begins.
	 * @param inplen The length of the data to encode.
	 * @param outbuf The Base64-encoded result.
	 * @return outbuf or a newly allocated {@link CharBuffer}
	 */
	public static CharBuffer encode(int[] input, int inpoff, int inplen, CharBuffer outbuf)
	{
		final int width = 3;
		final int remainder = inplen % width;
		final int outlen = Base64.getEncodedLength(inplen << 2);
		if ((outbuf == null) || (outbuf.capacity() < outlen)) {
			// Output buffer is too small, resize
			outbuf = CharBuffer.allocate(outlen);
		}
		else {
			// Output buffer is big enough
			outbuf.clear();
		}
		
		// Counters for input/output processing. (-1 needed for prefix-increments!)
		int outpos = outbuf.arrayOffset() + outbuf.position() - 1;
		int inpos = inpoff - 1;
		final int inend = inpos + inplen - remainder;
		final char[] output = outbuf.array();
		
		// Encode 3 integers into 16 Base64-characters at a time
		while (inpos < inend) {
			// Encode first 3 bytes of 1. integer: XXX* **** ****
			int bits = input[++inpos] >>> SHIFT_1x8BIT;
			outpos = Base64.encodeAs4x6Bit(output, outpos, bits);
			
			// Encode last byte of 1. int and first 2 bytes of 2. int: ***X XX** ****
			bits = (input[inpos] & MASK_8BIT) << SHIFT_2x8BIT;
			bits |= (input[++inpos] >>> SHIFT_2x8BIT) & MASK_16BIT;
			outpos = Base64.encodeAs4x6Bit(output, outpos, bits);
			
			// Encode last 2 bytes of 2. int and first byte of 3. int: **** **XX X*** 
			bits = (input[inpos] << SHIFT_1x8BIT) & MASK_24BIT;
			bits |= input[++inpos] >>> SHIFT_3x8BIT;
			outpos = Base64.encodeAs4x6Bit(output, outpos, bits);
			
			// Encode last 3 bytes of 3. int: **** **** *XXX 
			bits = input[inpos] & MASK_24BIT;
			outpos = Base64.encodeAs4x6Bit(output, outpos, bits);
		}
		
		// Encode the last integers, if inplen is not multiple of 3 
		if (remainder > 0) {
			long bits = 0;
			
			// Assemble the bit pattern from last integers
			for (int i = remainder, shift = SHIFT_4x8BIT; i > 0; --i, shift -= SHIFT_4x8BIT)
				bits |= (input[++inpos] & 0xFFFFFFFFL) << shift;

			// Construct the chars from bit pattern, ignoring the last 4 bits
			for (int i = 5 * remainder, shift = 9 * SHIFT_1x6BIT + 4; i > 0; --i, shift -= SHIFT_1x6BIT)
				output[++outpos] = CTABLE[(int)((bits >>> shift) & MASK_6BIT)];
			
			if (remainder == 1) {
				// Handle the last 2 bits of 1. integer
				bits = (bits << 4) >>> SHIFT_4x8BIT;
			}
			else {
				// Handle the last 4 bits of 2. integer
				bits = bits << 2;
			}
			
			// Final character
			output[++outpos] = CTABLE[(int)(bits & MASK_6BIT)];
			
			// Add padding chars
			while (++outpos < outlen)
				output[outpos] = PAD_CHAR;
		}
		
		outbuf.limit(outlen);
		return outbuf;
	}
	
	/**
	 * Decodes the Base64-encoded inpbuf and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param inpbuf The Base64-encoded input.
	 * @param outbuf The decoded bytes.
	 * @return outbuf or a newly allocated {@link ByteBuffer}
	 */
	public static ByteBuffer decode(CharBuffer inpbuf, ByteBuffer outbuf) throws IOException
	{
		final int inpoff = inpbuf.arrayOffset() + inpbuf.position();
		final int inplen = inpbuf.length();
		return Base64.decode(inpbuf.array(), inpoff, inplen, outbuf);
	}
	
	/**
	 * Decodes the Base64-encoded inpbuf and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param inpbuf The Base64-encoded input.
	 * @param outbuf The decoded bytes.
	 * @return outbuf or a newly allocated {@link ByteBuffer}
	 */
	public static ByteBuffer decode(CharArrayBuffer inpbuf, ByteBuffer outbuf) throws IOException
	{
		final int inpoff = inpbuf.offset();
		final int inplen = inpbuf.length();
		return Base64.decode(inpbuf.array(), inpoff, inplen, outbuf);
	}
	
	/**
	 * Decodes the Base64-encoded inpbuf and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param input The Base64-encoded input buffer.
	 * @param inpoff The offset, where the Base64-data inside the input begins.
	 * @param inplen The length of the Base64-data to decode.
	 * @param outbuf The decoded output bytes.
	 * @return outbuf or a newly allocated {@link ByteBuffer}
	 */
	public static ByteBuffer decode(char[] input, int inpoff, int inplen, ByteBuffer outbuf) throws IOException
	{
		final int width = 4;
		final int padnum = Base64.getNumPadCharacters(input, inpoff, inplen);
		final int outlen = Base64.getDecodedLength(inplen, padnum);
		if ((outbuf == null) || (outbuf.capacity() < outlen)) {
			// Output buffer is too small, resize
			outbuf = ByteBuffer.allocate(outlen);
		}
		else {
			// Output buffer is big enough
			outbuf.clear();
		}
		
		// Counters for input/output processing. (-1 needed for prefix-increments!)
		final byte[] output = outbuf.array();
		int outpos = outbuf.arrayOffset() + outbuf.position() - 1;
		final int inend = inpoff + inplen - 1 - ((padnum > 0) ? width : 0);
		int inpos = inpoff - 1;
		
		int b3, b2, b1, bits;
		
		// Decode all characters into bytes
		while (inpos < inend) {
			// Assemble the bit pattern from four encoded characters
			b3 = ITABLE[input[++inpos]] << SHIFT_3x6BIT;
			b2 = ITABLE[input[++inpos]] << SHIFT_2x6BIT;
			b1 = ITABLE[input[++inpos]] << SHIFT_1x6BIT;
			bits = b3 | b2 | b1 | ITABLE[input[++inpos]];
			
			// Construct the bytes from bit pattern
			output[++outpos] = (byte)  (bits >> SHIFT_2x8BIT);
			output[++outpos] = (byte) ((bits >> SHIFT_1x8BIT) & MASK_8BIT);
			output[++outpos] = (byte)  (bits & MASK_8BIT);
		}
		
		// Decode the last characters into bytes, handling the padding
		if (padnum > 0) {
			final int count = width - padnum;
			bits = 0;
			
			// Assemble the bit pattern from four encoded characters
			for (int i = count, shift = SHIFT_3x6BIT; i > 0; --i, shift -= SHIFT_1x6BIT)
				bits |= ITABLE[input[++inpos]] << shift;
			
			// Construct the bytes from bit pattern
			for (int i = count, shift = SHIFT_2x8BIT; i > 1; --i, shift -= SHIFT_1x8BIT)
				output[++outpos] = (byte) ((bits >> shift) & MASK_8BIT);
		}
		
		outbuf.limit(outlen);
		return outbuf;
	}
	
	/**
	 * Decodes the Base64-encoded inpbuf and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param inpbuf The Base64-encoded input.
	 * @param outbuf The decoded integers.
	 * @return outbuf or a newly allocated {@link IntBuffer}
	 */
	public static IntBuffer decode(CharBuffer inpbuf, IntBuffer outbuf) throws IOException
	{
		final int inpoff = inpbuf.arrayOffset() + inpbuf.position();
		final int inplen = inpbuf.length();
		return Base64.decode(inpbuf.array(), inpoff, inplen, outbuf);
	}
	
	/**
	 * Decodes the Base64-encoded inpbuf and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param inpbuf The Base64-encoded input.
	 * @param outbuf The decoded integers.
	 * @return outbuf or a newly allocated {@link IntBuffer}
	 */
	public static IntBuffer decode(CharArrayBuffer inpbuf, IntBuffer outbuf) throws IOException
	{
		final int inpoff = inpbuf.offset();
		final int inplen = inpbuf.length();
		return Base64.decode(inpbuf.array(), inpoff, inplen, outbuf);
	}
	
	/**
	 * Decodes the Base64-encoded inpbuf and stores the result in outbuf.
	 * When the capacity of outbuf is too small, then it will be resized.
	 * @param input The Base64-encoded input buffer.
	 * @param inpoff The offset, where the Base64-data inside the input begins.
	 * @param inplen The length of the Base64-data to decode.
	 * @param outbuf The decoded output integers.
	 * @return outbuf or a newly allocated {@link IntBuffer}
	 */
	public static IntBuffer decode(char[] input, int inpoff, int inplen, IntBuffer outbuf) throws IOException
	{
		final int width = 16;
		final int padnum = Base64.getNumPadCharacters(input, inpoff, inplen);
		final int remainder = (inplen - padnum) % width;
		final int outlen = Base64.getDecodedLength(inplen, padnum) >> 2;
		if ((outbuf == null) || (outbuf.capacity() < outlen)) {
			// Output buffer is too small, resize
			outbuf = IntBuffer.allocate(outlen);
		}
		else {
			// Output buffer is big enough
			outbuf.clear();
		}
		
		// Counters for input/output processing. (-1 needed for prefix-increments!)
		int outpos = outbuf.arrayOffset() + outbuf.position() - 1;
		int inpos = inpoff - 1;
		final int inend = inpos + inplen - ((padnum > 0) ? width : 0);
		final int[] output = outbuf.array();
		
		int b1, b2;
		
		// Decode all characters into integers
		while (inpos < inend) {
			// Decode first 3 bytes of 1. integer: XXX* **** ****
			b1 = Base64.decodeAs3x8Bit(input, inpos);
			inpos += 4;
			
			// Decode last byte of 1. int and first 2 bytes of 2. int: ***X XX** ****
			b2 = Base64.decodeAs3x8Bit(input, inpos);
			inpos += 4;
			
			// Construct 1. integer
			output[++outpos] = (b1 << SHIFT_1x8BIT) | (b2 >> SHIFT_2x8BIT);
			
			// Decode last 2 bytes of 2. int and first byte of 3. int: **** **XX X***
			b1 = Base64.decodeAs3x8Bit(input, inpos);
			inpos += 4;
			
			// Construct 2. integer
			output[++outpos] = (b2 << SHIFT_2x8BIT) | (b1 >> SHIFT_1x8BIT);
			
			// Decode last 3 bytes of 3. int: **** **** *XXX
			b2 = Base64.decodeAs3x8Bit(input, inpos);
			inpos += 4;
			
			// Construct 3. integer
			output[++outpos] = (b1 << SHIFT_3x8BIT) | b2;
		}
		
		// Handle the last characters and padding
		if (remainder > 0) {
			// Decode first 3 bytes of 1. integer: XXX* ****
			b1 = Base64.decodeAs3x8Bit(input, inpos);
			inpos += 4;

			// A single integer left?
			if (remainder < 7) {
				// Yes, decode last byte of 1. int: ***X ****
				b2  = ITABLE[input[++inpos]] << SHIFT_1x6BIT;
				b2 |= ITABLE[input[++inpos]];
	
				// Construct the integer
				output[++outpos] = (b1 << SHIFT_1x8BIT) | (b2 >> 4);
			}
			else {
				// No, 2 integers are left!
				
				// Decode last byte of 1. int and first 2 bytes of 2. int: ***X XX**
				b2 = Base64.decodeAs3x8Bit(input, inpos);
				inpos += 4;
				
				// Construct 1. integer
				output[++outpos] = (b1 << SHIFT_1x8BIT) | (b2 >> SHIFT_2x8BIT);
				
				// Decode last 2 bytes of 2. int and first byte of 3. int: **** **XX
				b1  = ITABLE[input[++inpos]] << SHIFT_2x6BIT;
				b1 |= ITABLE[input[++inpos]] << SHIFT_1x6BIT;
				b1 |= ITABLE[input[++inpos]];
				
				// Construct 2. integer
				output[++outpos] = (b2 << SHIFT_2x8BIT) | (b1 >> 2);
			}
		}
		
		outbuf.limit(outlen);
		return outbuf;
	}
	
	
	/* ========================= Internal Methods ========================= */
	
	/** Compute the number of padding chars '=' at the end of input. */
	private static int getNumPadCharacters(char[] inpbuf, int inpoff, int inplen)
	{
		final int endidx = inpoff + inplen - 1;
		final int padnum = (inpbuf[endidx] != PAD_CHAR) ? 0 :
			((inpbuf[endidx - 1] != PAD_CHAR) ? 1 : 2);
		
		return padnum;
	}
	
	/** Returns the decoded length in bytes of the Base64-encoded buffer. */
	private static int getDecodedLength(int inplen, int padnum) throws IOException
	{
		if (inplen % 4 != 0)
			throw new IOException("Length of Base64 encoded input is not a multiple of 4.");
		
		return (((inplen - padnum) * 3) >> 2);
	}
	
	/** Construct 4 encoded chars from 24 bits pattern. The MSB must be 0! */
	private static final int encodeAs4x6Bit(char[] output, int outpos, int bits)
	{
		output[++outpos] = CTABLE[bits >>> SHIFT_3x6BIT];
		output[++outpos] = CTABLE[(bits >>> SHIFT_2x6BIT) & MASK_6BIT];
		output[++outpos] = CTABLE[(bits >>> SHIFT_1x6BIT) & MASK_6BIT];
		output[++outpos] = CTABLE[bits & MASK_6BIT];
		return outpos;
	}
	
	/** Construct 3 decoded bytes from 4 chars. */
	private static final int decodeAs3x8Bit(char[] input, int inpos)
	{
		final int b3 = ITABLE[input[++inpos]] << SHIFT_3x6BIT;
		final int b2 = ITABLE[input[++inpos]] << SHIFT_2x6BIT;
		final int b1 = ITABLE[input[++inpos]] << SHIFT_1x6BIT;
		return (b3 | b2 | b1 | ITABLE[input[++inpos]]);
	}
}
