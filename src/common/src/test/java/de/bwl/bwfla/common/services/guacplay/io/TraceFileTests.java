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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.services.guacplay.BaseTest;
import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;


public class TraceFileTests extends BaseTest
{
	private static final TraceData DATA = new TraceData(4096, 8192);
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		final Logger log = LoggerFactory.getLogger(TraceFileTests.class);
		
		log.info("Generating trace-data...");
		DATA.generate();
		
		log.info("Writing temporary trace-file...");
		
		final Path path = Files.createTempFile("guacplay-", GuacDefs.TRACE_FILE_EXT);
		final TraceFile file = new TraceFile(path, StandardCharsets.UTF_8);
		final TraceFileWriter writer = file.newBufferedWriter();
		final TraceBlockWriter block = new TraceBlockWriter();
		final int msgnum = DATA.length();
		final int commentIndex = DATA.nextInt(0, msgnum);
		
		try {
			writer.prepare();
			writer.begin(block);
			
			// Write the block's entries
			for (int index = 0; index < msgnum; ++index) {
				if (index == commentIndex) {
					// Add a comment randomly!
					String comment = new String(DATA.nextCharArray(20, 30));
					block.comment(comment);
				}
				
				final Message msg = DATA.message(index);
				block.write(msg.getTimestamp(), msg.getDataArray(), msg.getOffset(), msg.getLength());
			}
			
			// Write the metadata
			Metadata metadata = file.getMetadata();
			for (MetadataChunk chunk : DATA.metadata().getChunks())
				metadata.addChunk(chunk);
			
			writer.finish();
			
			log.info("{} bytes written to: {}", writer.getNumBytesWritten(), path.toString());
		}
		finally {
			// Always close the writer!
			writer.close();
		}
		
		DATA.setTraceFile(file);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		if (TESTS_PASSED) {
			final Logger log = LoggerFactory.getLogger(TraceFileTests.class);
			final TraceFile file = DATA.getTraceFile();
			log.info("Deleting temporary trace-file...");
			Files.deleteIfExists(file.getPath());
			log.info("Deleted {}", file.getPath().toString());
		}
		
		BaseTest.tearDownAfterClass();
	}
	
	@Test
	public void testReadingIndexBlock() throws IOException
	{
		log.info("Testing index-block...");
		
		final TraceFile file = DATA.getTraceFile();
		final TraceFileReader reader = file.newBufferedReader();
		
		try {
			reader.prepare(false);
			
			// Check existence of blocks
			Assert.assertTrue("Trace block was not found!", reader.contains(TraceBlockWriter.BLOCKNAME));
			Assert.assertTrue("Metadata block was not found!", reader.contains(TraceFileDefs.BLOCKNAME_METADATA));
			
			// Check trace-block offset
			TraceBlockReader block = new TraceBlockReader();
			reader.begin(block);
			reader.finish();
		}
		finally {
			// Always close the reader!
			reader.close();
		}
		
		this.markAsPassed();
	}
	
	@Test
	public void testReadingMetadataBlock() throws IOException
	{
		log.info("Testing metadata-block...");
		
		final TraceFile file = DATA.getTraceFile();
		final TraceFileReader reader = file.newBufferedReader();
		
		try {
			reader.prepare(true);
			
			// Check existence of blocks
			Assert.assertTrue("Metadata block was not found!", reader.contains(TraceFileDefs.BLOCKNAME_METADATA));
			
			// Check metadata chunks
			final Metadata metadata = file.getMetadata();
			for (MetadataChunk expchunk : DATA.metadata().getChunks()) {
				final String ctag = expchunk.getTag();
				if (!metadata.containsChunk(ctag))
					Assert.fail("MetadataChunk '" + ctag + "' not found!");
				
				// Check chunk's entries
				final MetadataChunk curchunk = metadata.getChunk(ctag);
				for (Entry<String, String> expentry : expchunk.entrySet()) {
					final String expkey = expentry.getKey();
					if (!curchunk.containsKey(expkey))
						Assert.fail("MetadataChunk '" + ctag + "' does't have key '" + expkey + "'!");
					
					final String expval = expentry.getValue();
					final String curval = curchunk.get(expkey);
					if (!curval.contentEquals(expval)) {
						String message = "Invalid value found for key '" + expkey
								+ "'!\nExpected: '" + expval + "'\nActual: '" + curval + "'";
						Assert.fail(message);
					}
				}
				
				// Check optional comment
				final String expcom = expchunk.getComment();
				final String curcom = curchunk.getComment();
				if (expcom == null && curcom != null)
					Assert.fail("A comment for chunk '" + ctag + "' was not written!");
				else if (expcom != null && curcom == null)
					Assert.fail("A comment for chunk '" + ctag + "' is missing!");
				else if (expcom == null && curcom == null)
					continue;
				
				if (!curcom.contentEquals(expcom)) {
					String message = "Invalid comment found for chunk '" + ctag + "'!\nExpected: '"
							+ expcom + "'\nActual: '" + curcom + "'";
					Assert.fail(message);
				}
			}
		}
		finally {
			// Always close the reader!
			reader.close();
		}
		
		this.markAsPassed();
	}
	
	@Test
	public void testReadingTraceBlock() throws IOException
	{
		log.info("Testing trace-block...");
		
		final TraceFile file = DATA.getTraceFile();
		final TraceFileReader reader = file.newBufferedReader();
		
		try {
			reader.prepare(false);
			
			// Check existence of block
			Assert.assertTrue("Trace block was not found!", reader.contains(TraceBlockWriter.BLOCKNAME));
			
			// Check trace-block offset
			TraceBlockReader block = new TraceBlockReader();
			reader.begin(block);
			
			int index = 0;
			
			// Check trace-block content
			final Message curmsg = new Message();
			while (block.read(curmsg)) {
				Assert.assertTrue(index < DATA.length());
				
				final Message expmsg = DATA.message(index);
				Assert.assertEquals("Invalid timestamp read!", curmsg.getTimestamp(), expmsg.getTimestamp());
				Assert.assertEquals("Invalid message's length read!", curmsg.getLength(), expmsg.getLength());
				
				final char[] curarray = curmsg.getDataArray();
				final char[] exparray = expmsg.getDataArray();
				final int imax = curmsg.getOffset() + curmsg.getLength();
				for (int i = curmsg.getOffset(), j = expmsg.getOffset(); i < imax; ++i, ++j) {
					if (curarray[i] != exparray[j]) {
						final String curstr = new String(curarray, curmsg.getOffset(), curmsg.getLength());
						final String expstr = new String(exparray, expmsg.getOffset(), expmsg.getLength());
						Assert.fail("Invalid char found at pos " + (i - curmsg.getOffset()) + " in: '" + curstr + "' vs '" + expstr + "'.");
					}
				}
				
				++index;  // Next message
			}
			
			Assert.assertEquals("Invalid number of messages read/written!", index, DATA.length());
			
			reader.finish();
		}
		finally {
			// Always close the reader!
			reader.close();
		}
		
		this.markAsPassed();
	}
	
	
	/* =============== INTERNAL STUFF =============== */
	
	private static final class TraceData
	{
		private final Random random;
		private final Message[] messages;
		private final Metadata metadata;
		private TraceFile file;
		
		private static final char[] CHARS
				= "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		
		
		public TraceData(int min, int max)
		{
			this.random = new Random();
			this.messages = new Message[this.nextInt(min, max)];
			this.metadata = new Metadata();
			this.file = null;
		}
		
		public void generate()
		{
			long timestamp = random.nextInt(Integer.MAX_VALUE / 1000);
			
			// Randomly generate the message-data
			for (int i = 0; i < messages.length; ++i) {
				char[] data = this.nextInstruction(16, 64);
				messages[i] = new Message();
				messages[i].set(SourceType.INTERNAL, timestamp, data, 0, data.length);
				
				timestamp += random.nextInt(1000);
			}
			
			final Logger log = LoggerFactory.getLogger(TraceFileTests.class);
			log.info("{} trace-events generated.", messages.length);
			
			// Randomly generate metadata
			final int numChunks = this.nextInt(1, 10);
			for (int i = 0; i < numChunks; ++i) {
				final String ctag = this.nextString(5, 10) + i;
				final String comment = (random.nextBoolean()) ? this.nextString(10, 20) : null;
				final MetadataChunk chunk = new MetadataChunk(ctag, comment);
				
				final int numEntries = this.nextInt(5, 50);
				for (int j = 0; j < numEntries; ++j) {
					String key = this.nextString(5, 10) + j;
					String value = null;
					
					final int type = this.nextInt(0, 4);
					switch (type) {
						case 0: {  /* Single-line string */
							value = this.nextString(3, 50);
							break;
						}
						case 1: {  /* Multi-line string */
							char[] data = this.nextCharArray(25, 100);
							final int n = data.length / 10;
							for (int k = 0; k < n; ++k) {
								int index = this.nextInt(1, data.length);
								data[index] = (random.nextBoolean()) ? '\n' : ' ';
							}
							value = new String(data, 0, data.length);
							break;
						}
						case 2: {  /* Integer */
							int number = random.nextInt();
							value = Integer.toString(number);
							break;
						}
						case 3: {  /* Float */
							float number = random.nextFloat();
							value = Float.toString(number);
							break;
						}
					}
					
					chunk.put(key, value);
				}
				
				metadata.addChunk(chunk);
			}
		}
		
		public Metadata metadata()
		{
			return metadata;
		}
		
		public Message message(int index)
		{
			return messages[index];
		}
		
		public int length()
		{
			return messages.length;
		}
		
		public char[] nextCharArray(int min, int max)
		{
			final int length = this.nextInt(min, max);
			final char[] data = new char[length];
			
			for (int i = 0; i < length; ++i) {
				int index = random.nextInt(CHARS.length);
				data[i] = CHARS[index];
			}

			return data;
		}
		
		public char[] nextInstruction(int min, int max)
		{
			final char[] data = this.nextCharArray(min, max);
			final int last = data.length - 1;
			data[last] = GuacDefs.INSTRUCTION_TERMINATOR;
			return data;
		}
		
		public String nextString(int min, int max)
		{
			char[] data = this.nextCharArray(min, max);
			return new String(data, 0, data.length);
		}
		
		public final int nextInt(int min, int max)
		{
			return (min + random.nextInt(max - min));
		}
		
		public void setTraceFile(TraceFile file)
		{
			this.file = file;
		}
		
		public TraceFile getTraceFile()
		{
			return file;
		}
	}
}
