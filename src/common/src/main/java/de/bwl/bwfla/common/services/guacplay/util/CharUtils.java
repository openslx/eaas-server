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

import java.nio.CharBuffer;


/** Utility methods for plain char-arrays. */
public final class CharUtils
{
	/**
	 * Find the first occurence of the specified character in the data-array.
	 * @param array The array to search in.
	 * @param start The start position of the search.
	 * @param end The end postion of the search.
	 * @param symbol The symbol to find.
	 * @return The index of the symbol when found, else -1.
	 */
	public static int indexOf(char[] array, int start, int end, char symbol)
	{
		for (int pos = start; pos < end; ++pos) {
			if (array[pos] == symbol)
				return pos;  // Found!
		}
		
		return -1;  // Not found!
	}
		
	/**
	 * Find the first occurence of any of the specified characters in the data-array.
	 * @param array The array to search in.
	 * @param start The start position of the search.
	 * @param end The end postion of the search.
	 * @param symbols The symbols to find.
	 * @return The index of one of the symbols when found, else -1.
	 */
	public static int indexOfAny(char[] array, int start, int end, char... symbols)
	{
		for (int pos = start; pos < end; ++pos) {
			// Compare with all symbols to find
			for (char symbol : symbols) {
				if (array[pos] == symbol)
					return pos;  // Found!
			}
		}
		
		return -1;  // Not found!
	}
	
	/**
	 * Find the first occurence of the specified character in the data-array.
	 * @param buffer The wrapper for the array to search in.
	 * @param symbol The symbol to find.
	 * @return The index of the symbol when found, else -1. 
	 * @see #indexOf(char[], int, int, char)
	 */
	public static int indexOf(CharArrayBuffer buffer, char symbol)
	{
		final char[] array = buffer.array();
		final int start = buffer.position();
		final int end = buffer.limit();
		
		return CharUtils.indexOf(array, start, end, symbol);
	}
	
	/**
	 * Find the first occurence of one of the specified characters in the data-array.
	 * @param buffer The wrapper for the array to search in.
	 * @param symbols The symbols to find.
	 * @return The index of one of the symbols when found, else -1. 
	 * @see #indexOf(char[], int, int, char...)
	 */
	public static int indexOfAny(CharArrayBuffer buffer, char... symbols)
	{
		final char[] array = buffer.array();
		final int start = buffer.position();
		final int end = buffer.limit();
		
		return CharUtils.indexOfAny(array, start, end, symbols);
	}
	
	/**
	 * Find the first occurence of the specified character in the buffer.
	 * @param buffer The buffer to search in.
	 * @param symbol The symbol to find.
	 * @return The index of the symbol when found, else -1. 
	 */
	public static int indexOf(CharBuffer buffer, char symbol)
	{
		final char[] array = buffer.array();
		final int offset = buffer.arrayOffset();
		final int start = offset + buffer.position();
		final int end = offset + buffer.limit();
		
		return (CharUtils.indexOf(array, start, end, symbol) - offset);
	}
	
	/**
	 * Find the first occurence of the specified character in the buffer. <p/>
	 * <b>NOTE:</b> The buffer's array-offset is assumed to be 0!
	 * @param buffer The buffer to search in.
	 * @param symbol The symbol to find.
	 * @return The index of the symbol when found, else -1. 
	 */
	public static int indexOfZeroBased(CharBuffer buffer, char symbol)
	{
		final int start = buffer.position();
		final int end = buffer.limit();
		
		return CharUtils.indexOf(buffer.array(), start, end, symbol);
	}
	
	/** 
	 * Test, whether the specified array contains the string.
	 * @param buffer The buffer to look in.
	 * @param string The string to look for.
	 * @return true when buffer contains the string, else false.
	 */
	public static boolean contains(char[] array, int start, int end, String string)
	{
		final int length = string.length();
		
		// Check the string's length
		if ((end - start) < length)
			return false;  // Too small
				
		// Check array's content
		for (int strpos = 0, arrpos = start; strpos < length; ++strpos, ++arrpos) {
			if (array[arrpos] != string.charAt(strpos))
				return false;  // Does not contain the string!
		}
		
		return true;  // String found!
	}
	
	/** 
	 * Test, whether the specified buffer contains the string at its current position.
	 * @param buffer The buffer to look in.
	 * @param string The string to look for.
	 * @return true when buffer contains the string, else false.
	 */
	public static boolean contains(CharBuffer buffer, String string)
	{
		final int offset = buffer.arrayOffset();
		final int start = offset + buffer.position();
		final int end = offset + buffer.limit();

		return CharUtils.contains(buffer.array(), start, end, string);
	}
	
	/** 
	 * Test, whether the specified buffer contains the string at its current position. <p/>
	 * <b>NOTE:</b> The buffer's array-offset is assumed to be 0!
	 * @param buffer The buffer to look in.
	 * @param string The string to look for.
	 * @return true when buffer contains the string, else false.
	 */
	public static boolean containsZeroBased(CharBuffer buffer, String string)
	{
		final int start = buffer.position();
		final int end = buffer.limit();

		return CharUtils.contains(buffer.array(), start, end, string);
	}
	
	/**
	 * Skip all of the specified subsequent symbols.
	 * @param array The array containing data.
	 * @param pos The start position of the search.
	 * @param end The end position of the search.
	 * @param symbol The symbol to skip.
	 * @return The updated position, after skipping.
	 */
	public static int skip(char[] array, int pos, int end, char symbol)
	{
		// Skip the specified symbols
		while ((pos < end) && (array[pos] == symbol))
			++pos;  // Goto next character
		
		return pos;
	}
	
	/**
	 * Skip all of the specified subsequent symbols.
	 * @param array The array containing data.
	 * @param pos The start position of the search.
	 * @param end The end position of the search.
	 * @param sym1 The symbol to skip.
	 * @param sym2 The symbol to skip.
	 * @return The updated position, after skipping.
	 */
	public static int skip(char[] array, int pos, int end, char sym1, char sym2)
	{
		// Skip the specified symbols
		while (pos < end) {
			if ((array[pos] != sym1) && (array[pos] != sym2))
				break;  // No specified symbols found!
			
			++pos;
		}
		
		return pos;
	}
	
	/**
	 * Skip all of the specified subsequent symbols.
	 * @param array The array containing data.
	 * @param pos The start position of the search.
	 * @param end The end position of the search.
	 * @param symbols The symbols to skip.
	 * @return The updated position, after skipping.
	 */
	public static int skip(char[] array, int pos, int end, char... symbols)
	{
		boolean found = false;
		
		// Skip the specified symbols
		while (pos < end) {
			final char character = array[pos];
			for (char symbol : symbols) {
				if (character == symbol) {
					found = true;
					break;
				}
			}
			
			if (!found)
				break;  // No specified symbols found!
			
			// Goto next character
			found = false;
			++pos;
		}
		
		return pos;
	}
	
	/**
	 * Skip all of the specified subsequent symbols,
	 * updating wrapper's position accordingly.
	 * @param buffer The wrapper for the array.
	 * @param symbol The symbol to skip.
	 */
	public static void skip(CharArrayBuffer buffer, char symbol)
	{
		final char[] array = buffer.array();
		final int start = buffer.position();
		final int end = buffer.limit();
		
		buffer.setPosition(CharUtils.skip(array, start, end, symbol));
	}
	
	/**
	 * Skip all of the specified subsequent symbols,
	 * updating wrapper's position accordingly.
	 * @param buffer The wrapper for the array.
	 * @param sym1 The symbol to skip.
	 * @param sym2 The symbol to skip.
	 */
	public static void skip(CharArrayBuffer buffer, char sym1, char sym2)
	{
		final char[] array = buffer.array();
		final int start = buffer.position();
		final int end = buffer.limit();
		
		buffer.setPosition(CharUtils.skip(array, start, end, sym1, sym2));
	}
	
	/**
	 * Skip all of the specified subsequent symbols,
	 * updating wrapper's position accordingly.
	 * @param buffer The wrapper for the array.
	 * @param symbols The symbols to skip.
	 */
	public static void skip(CharArrayBuffer buffer, char... symbols)
	{
		final char[] array = buffer.array();
		final int start = buffer.position();
		final int end = buffer.limit();
		
		buffer.setPosition(CharUtils.skip(array, start, end, symbols));
	}

	/**
	 * Skip the specified number of chars, by advancing buffer's position.
	 * @param buffer The buffer to update.
	 * @param count The number of chars to skip.
	 */
	public static void skip(CharBuffer buffer, int count)
	{
		count = Math.min(count, buffer.remaining());
		buffer.position(buffer.position() + count);
	}
	
	/**
	 * Skip the same subsequent characters, starting at buffer's current position.
	 * @param buffer The buffer to update.
	 * @param symbol The character to skip.
	 */
	public static void skip(CharBuffer buffer, char symbol)
	{
		final int offset = buffer.arrayOffset();
		final int start = offset + buffer.position();
		final int end = offset + buffer.limit();
		final int pos = CharUtils.skip(buffer.array(), start, end, symbol);
		
		buffer.position(pos - offset);
	}

	/**
	 * Skip all of the specified subsequent characters, starting at buffer's current position.
	 * @param buffer The buffer to update.
	 * @param sym1 The character to skip.
	 * @param sym2 The character to skip.
	 */
	public static void skip(CharBuffer buffer, char sym1, char sym2)
	{
		final int offset = buffer.arrayOffset();
		final int start = offset + buffer.position();
		final int end = offset + buffer.limit();
		final int pos = CharUtils.skip(buffer.array(), start, end, sym1, sym2);
		
		buffer.position(pos - offset);
	}

	/**
	 * Skip all of the specified subsequent characters, starting at buffer's current position.
	 * @param buffer The buffer to update.
	 * @param symbols The characters to skip.
	 */
	public static void skip(CharBuffer buffer, char... symbols)
	{
		final int offset = buffer.arrayOffset();
		final int start = offset + buffer.position();
		final int end = offset + buffer.limit();
		final int pos = CharUtils.skip(buffer.array(), start, end, symbols);
		
		buffer.position(pos - offset);
	}
	
	/**
	 * Skip the same subsequent characters, starting at buffer's current position. <p/>
	 * <b>NOTE:</b> The buffer's array-offset is assumed to be 0!
	 * @param buffer The buffer to update.
	 * @param symbol The character to skip.
	 */
	public static void skipZeroBased(CharBuffer buffer, char symbol)
	{
		final int start = buffer.position();
		final int end = buffer.limit();
		final int pos = CharUtils.skip(buffer.array(), start, end, symbol);
		
		buffer.position(pos);
	}
	
	/**
	 * Skip all of the specified subsequent characters, starting at buffer's current position. <p/>
	 * <b>NOTE:</b> The buffer's array-offset is assumed to be 0!
	 * @param buffer The buffer to update.
	 * @param sym1 The character to skip.
	 * @param sym2 The character to skip.
	 */
	public static void skipZeroBased(CharBuffer buffer, char sym1, char sym2)
	{
		final int start = buffer.position();
		final int end = buffer.limit();
		final int pos = CharUtils.skip(buffer.array(), start, end, sym1, sym2);
		
		buffer.position(pos);
	}
	
	/**
	 * Skip all of the specified subsequent characters, starting at buffer's current position. <p/>
	 * <b>NOTE:</b> The buffer's array-offset is assumed to be 0!
	 * @param buffer The buffer to update.
	 * @param symbols The characters to skip.
	 */
	public static void skipZeroBased(CharBuffer buffer, char... symbols)
	{
		final int start = buffer.position();
		final int end = buffer.limit();
		final int pos = CharUtils.skip(buffer.array(), start, end, symbols);
		
		buffer.position(pos);
	}
	
	/**
	 * Create a substring from the specified buffer at its current position.
	 * @param buffer The buffer to create the string from.
	 * @param maxlen The maximum length of buffer's content to use.
	 * @return The created substring.
	 */
	public static String substring(CharBuffer buffer, int maxlen)
	{
		final int offset = buffer.arrayOffset() + buffer.position();
		if (buffer.remaining() < maxlen) {
			// No truncation needed
			return new String(buffer.array(), offset, buffer.remaining());
		}
		else {
			// Trancate the string
			StringBuffer strbuf = new StringBuffer(maxlen + 3);
			strbuf.append(buffer.array(), offset, maxlen);
			strbuf.append("...");
			return strbuf.toString();
		}
	}
}
