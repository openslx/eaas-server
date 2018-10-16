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

package de.bwl.bwfla.common.services.guacplay.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import org.junit.Test;

import de.bwl.bwfla.common.services.guacplay.BaseTest;
import de.bwl.bwfla.common.services.guacplay.util.Base64;

import static org.junit.Assert.*;


public class Base64Test extends BaseTest
{
	private static final String[] DECODED_DATA = {
			"Man",
			"any carnal pleasure.",
			"any carnal pleasure",
			"any carnal pleasur",
			"any carnal pleasu",
			"any carnal pleas",
			"pleasure.",
			"leasure.",
			"easure.",
			"asure.",
			"sure.",
			"Man is distinguished, not only by his reason, but by this singular passion from " +
					"other animals, which is a lust of the mind, that by a perseverance of delight " +
					"in the continued and indefatigable generation of knowledge, exceeds the short " +
					"vehemence of any carnal pleasure."
		};

	private static final String[] ENCODED_DATA = {
			"TWFu",
			"YW55IGNhcm5hbCBwbGVhc3VyZS4=",
			"YW55IGNhcm5hbCBwbGVhc3VyZQ==",
			"YW55IGNhcm5hbCBwbGVhc3Vy",
			"YW55IGNhcm5hbCBwbGVhc3U=",
			"YW55IGNhcm5hbCBwbGVhcw==",
			"cGxlYXN1cmUu",
			"bGVhc3VyZS4=",
			"ZWFzdXJlLg==",
			"YXN1cmUu",
			"c3VyZS4=",
			"TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz" + 
					"IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg" +
					"dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu" +
					"dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo" +
					"ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4="
		};
	
	private static final int NUMRUNS = 100000;

	
	@Test
	public void testEncodeBytes()
	{
		CharBuffer charbuf = CharBuffer.allocate(2);
		ByteBuffer bytebuf = ByteBuffer.allocate(2);
				
		// Encoding test
		log.info("Testing Bytes-Encoding...");
		for (int i = 0; i < ENCODED_DATA.length; ++ i) {
			byte[] data = DECODED_DATA[i].getBytes();
			bytebuf = ByteBuffer.wrap(data);
			
			// Compare string
			charbuf = Base64.encode(bytebuf, charbuf);
			assertTrue("Test #" + i + " failed!", ENCODED_DATA[i].contentEquals(charbuf));
		}
		
		this.markAsPassed();
	}
	
	@Test
	public void testDecodeBytes() throws IOException
	{
		CharBuffer charbuf = CharBuffer.allocate(2);
		ByteBuffer bytebuf = ByteBuffer.allocate(2);
		
		// Decoding test
		log.info("Testing Bytes-Decoding...");
		for (int i = 0; i < DECODED_DATA.length; ++ i) {
			final char[] chardata = ENCODED_DATA[i].toCharArray();
			charbuf = CharBuffer.wrap(chardata);
			bytebuf = Base64.decode(charbuf, bytebuf);
			
			// Compare length
			final byte[] bytedata = DECODED_DATA[i].getBytes();
			assertEquals("Invalid decoded length!", bytedata.length, bytebuf.remaining());
			
			// Compare bytes
			for (int j = 0; j < bytedata.length; ++j)
				assertEquals("Invalid decoded content!", bytedata[j], bytebuf.get(j));
		}
		
		this.markAsPassed();
	}
	
	@Test
	public void testEncodeIntegers() throws IOException
	{
		log.info("Testing Integers-Encoding...");
		
		for (int i = 0; i < NUMRUNS; ++i) {
			final int length = Base64Test.newRandomLength();
			final IntBuffer intbuf = IntBuffer.allocate(length);
			final ByteBuffer bytebuf = ByteBuffer.allocate(length * 4);
			Base64Test.fillBuffers(intbuf, bytebuf);
			
			final CharBuffer charbuf = Base64.encode(intbuf, null);
			final String refstr = org.apache.commons.codec.binary.Base64.encodeBase64String(bytebuf.array());
			
			// Compare length and content
			assertEquals("Invalid encoded length!", refstr.length(), charbuf.length());
			assertTrue("Invalid encoded content!", refstr.contentEquals(charbuf));
		}
		
		this.markAsPassed();
	}
	
	@Test
	public void testDecodeIntegers() throws IOException
	{
		log.info("Testing Integers-Decoding...");
		
		for (int i = 0; i < NUMRUNS; ++i) {
			final int length = Base64Test.newRandomLength();
			final IntBuffer intbuf = IntBuffer.allocate(length);
			final ByteBuffer bytebuf = ByteBuffer.allocate(length * 4);
			Base64Test.fillBuffers(intbuf, bytebuf);
			
			IntBuffer result = null;
			result = Base64.decode(Base64.encode(intbuf, null), result);
			
			// Compare length
			assertEquals("Invalid decoded length!", length, result.remaining());
				
			// Compare content
			for (int j = 0; j < length; ++j)
				assertEquals("Invalid decoded content!", intbuf.get(j), result.get(j));
		}
		
		this.markAsPassed();
	}
	
	
	/* ==================== INTERNAL METHODS ==================== */
	
	private static final int newRandomLength() 
	{
		final Random random = new Random();
		return (200 + random.nextInt(100));
	}
	
	private static final void fillBuffers(IntBuffer intbuf, ByteBuffer bytebuf)
	{
		intbuf.clear();
		bytebuf.clear();
		
		final Random random = new Random();
		final int length = intbuf.capacity();
		
		for (int j = 0; j < length; ++j) {
			int number = random.nextInt();
			bytebuf.putInt(number);
			intbuf.put(number);
		}
		
		intbuf.flip();
		bytebuf.flip();
	}
}
