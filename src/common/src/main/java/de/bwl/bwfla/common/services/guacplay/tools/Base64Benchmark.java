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

package de.bwl.bwfla.common.services.guacplay.tools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


public class Base64Benchmark
{
	private static final byte[][] DECODED_DATA = {
		"Man".getBytes(),
		"any carnal pleasure.".getBytes(),
		"any carnal pleasure".getBytes(),
		"any carnal pleasur".getBytes(),
		"any carnal pleasu".getBytes(),
		"any carnal pleas".getBytes(),
		"pleasure.".getBytes(),
		"leasure.".getBytes(),
		"easure.".getBytes(),
		"asure.".getBytes(),
		"sure.".getBytes(),
		("Man is distinguished, not only by his reason, but by this singular passion from " +
				"other animals, which is a lust of the mind, that by a perseverance of delight " +
				"in the continued and indefatigable generation of knowledge, exceeds the short " +
				"vehemence of any carnal pleasure.").getBytes()
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
	
	private static final String[] PACKAGES = {
		"org.apache.commons.codec.binary.Base64",
//		"org.jboss.resteasy.util.Base64",
		"de.bwl.bwfla.common.services.guacplay.util.Base64"
	};
	
	private static final int PACKAGE_ID_APACHE   = 0;
//	private static final int PACKAGE_ID_JBOSS    = 1;
	private static final int PACKAGE_ID_GUACPLAY = 1;
	
	private static final String SPEEDUP_FORMAT = "vs. %1s = %2.1fx\n";
	
	private static final int NUM_ITERATIONS = 500000;
	
	
	public static void main(String[] args) throws IOException
	{
		double[] enctimes = new double[PACKAGES.length];
		double[] dectimes = new double[PACKAGES.length];
		int totalBytesDecoded = 0;
		
		totalBytesDecoded += Base64Benchmark.benchmarkApacheBase64(enctimes, dectimes);
//		totalBytesDecoded += Base64Benchmark.benchmarkJbossBase64(enctimes, dectimes);
		totalBytesDecoded += Base64Benchmark.benchmarkGuacplayBase64(enctimes, dectimes);
		
		System.out.println("Decoding-Speedup of " + PACKAGES[PACKAGE_ID_GUACPLAY] + ":");
		for (int i = 0; i < PACKAGES.length - 1; ++i) {
			double speedup = dectimes[i] / dectimes[PACKAGE_ID_GUACPLAY];
			System.out.printf(SPEEDUP_FORMAT, PACKAGES[i], speedup);
		}
		
		System.out.println();
		System.out.println("Encoding-Speedup of " + PACKAGES[PACKAGE_ID_GUACPLAY] + ":");
		for (int i = 0; i < PACKAGES.length - 1; ++i) {
			double speedup = enctimes[i] / enctimes[PACKAGE_ID_GUACPLAY];
			System.out.printf(SPEEDUP_FORMAT, PACKAGES[i], speedup);
		}
	}
	
	
//	private static int benchmarkJbossBase64(double[] enctimes, double[] dectimes) throws IOException
//	{
//		System.out.println("Benchmarking the " + PACKAGES[PACKAGE_ID_JBOSS] + " implementation...");
//		
//		Timer timer = new Timer();
//		int totalBytesDecoded = 0;
//		
//		// ----- DECODING -----
//		timer.start();
//		for (int i = 0; i < NUM_ITERATIONS; ++i) {
//			for (int j = 0; j < ENCODED_DATA.length; ++j) {
//				byte[] bytes = org.jboss.resteasy.util.Base64.decode(ENCODED_DATA[j]);
//				totalBytesDecoded += bytes.length;
//			}
//		}
//		
//		dectimes[PACKAGE_ID_JBOSS] = timer.stop();
//		System.out.println("Decoding Time:  " + (long) dectimes[PACKAGE_ID_JBOSS] + " ns");
//		
//		// ----- ENCODING -----
//		timer.start();
//		for (int i = 0; i < NUM_ITERATIONS; ++i) {
//			for (int j = 0; j < DECODED_DATA.length; ++j) {
//				String str = org.jboss.resteasy.util.Base64.encodeBytes(DECODED_DATA[j]);
//				totalBytesDecoded += str.length();
//			}
//		}
//		
//		enctimes[PACKAGE_ID_JBOSS] = timer.stop();
//		System.out.println("Encoding Time:  " + (long) enctimes[PACKAGE_ID_JBOSS] + " ns");
//		System.out.println();
//		System.gc();
//		
//		return totalBytesDecoded;
//	}
	
	private static int benchmarkApacheBase64(double[] enctimes, double[] dectimes)
	{
		System.out.println("Benchmarking the " + PACKAGES[PACKAGE_ID_APACHE] + " implementation...");
		
		Timer timer = new Timer();
		int totalBytesDecoded = 0;
		
		// ----- DECODING -----
		timer.start();
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			for (int j = 0; j < ENCODED_DATA.length; ++j) {
				byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(ENCODED_DATA[j]);
				totalBytesDecoded += bytes.length;
			}
		}
		
		dectimes[PACKAGE_ID_APACHE] = timer.stop();
		System.out.println("Decoding Time:  " + (long) dectimes[PACKAGE_ID_APACHE] + " ns");
		
		// ----- ENCODING -----
		timer.start();
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			for (int j = 0; j < DECODED_DATA.length; ++j) {
				byte[] bytes = org.apache.commons.codec.binary.Base64.encodeBase64(DECODED_DATA[j]);
				totalBytesDecoded += bytes.length;
			}
		}
		
		enctimes[PACKAGE_ID_APACHE] = timer.stop();
		System.out.println("Encoding Time:  " + (long) enctimes[PACKAGE_ID_APACHE] + " ns");
		System.out.println();
		System.gc();
		
		return totalBytesDecoded;
	}
	
	private static int benchmarkGuacplayBase64(double[] enctimes, double[] dectimes) throws IOException
	{
		System.out.println("Benchmarking the " + PACKAGES[PACKAGE_ID_GUACPLAY] + " implementation...");
		
		ByteBuffer bytebuf = ByteBuffer.allocate(512);
		CharBuffer charbuf = CharBuffer.allocate(512);
		Timer timer = new Timer();
		int totalBytesDecoded = 0;
		
		char[][] chars = new char[ENCODED_DATA.length][];
		for (int j = 0; j < ENCODED_DATA.length; ++j)
			chars[j] = ENCODED_DATA[j].toCharArray();
		
		// ----- DECODING -----
		timer.start();
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			for (int j = 0; j < ENCODED_DATA.length; ++j) {
				//char[] chars = ENCODED_DATA[j].toCharArray();
				bytebuf = de.bwl.bwfla.common.services.guacplay.util.Base64.decode(chars[j], 0, chars[j].length, bytebuf);
				totalBytesDecoded += bytebuf.remaining();
			}
		}
		
		dectimes[PACKAGE_ID_GUACPLAY] = timer.stop();
		System.out.println("Decoding Time:  " + (long) dectimes[PACKAGE_ID_GUACPLAY] + " ns");
		
		// ----- ENCODING -----
		timer.start();
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			for (int j = 0; j < DECODED_DATA.length; ++j) {
				charbuf = de.bwl.bwfla.common.services.guacplay.util.Base64.encode(DECODED_DATA[j], 0, DECODED_DATA[j].length, charbuf);
				totalBytesDecoded += charbuf.length();
			}
		}
		
		enctimes[PACKAGE_ID_GUACPLAY] = timer.stop();
		System.out.println("Encoding Time:  " + (long) enctimes[PACKAGE_ID_GUACPLAY] + " ns");
		System.out.println();
		System.gc();
		
		return totalBytesDecoded;
	}
	
	
	private static class Timer
	{
		private long timestamp;
		
		public void start()
		{
			timestamp = System.nanoTime();
		}
		
		public double stop()
		{
			long duration = System.nanoTime() - timestamp;
			return (double) duration;
		}
	}
}
