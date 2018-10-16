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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.io.TraceBlockReader;
import de.bwl.bwfla.common.services.guacplay.io.TraceFile;
import de.bwl.bwfla.common.services.guacplay.io.TraceFileReader;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionParser;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.util.TimeUtils;


public final class TraceStats
{
	private static final String INDENT_1X = "    ";
	private static final String INDENT_2X = INDENT_1X + INDENT_1X;
	
	
	/** CLI-Argument: name of the trace-file */
	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("Trace-Filename not specified!");
			return;
		}
		
		final Path path = Paths.get(args[0]).toAbsolutePath();
		System.out.println("Trace-File: " + path.toString());
		
		final TraceFile file = new TraceFile(path, StandardCharsets.UTF_8);
		final TraceFileReader reader = file.newBufferedReader();
		final TraceBlockReader block = new TraceBlockReader();
		
		try {
			reader.prepare();
			reader.begin(block);
			
			TraceStats.printFileSize(reader.size());
			TraceStats.printTraceStats(block);
		}
		finally {
			// Always close the reader!
			reader.close();
		}
		
		System.out.println("Done!");
		System.out.println();
	}
	
	
	/* ==================== Internal Methods ==================== */
	
	private static void printFileSize(int size)
	{
		final int KB = 1024;
		final int MB = KB * KB;
		
		String sizestr = new String(size + " bytes");
		if (size < MB)
			sizestr += String.format(" (%1.2f KB)", ((double) size / (double) KB));
		else sizestr += String.format(" (%1.2f MB)", ((double) size / (double) MB));

		System.out.println("File-Size: " + sizestr);
	}
	
	private static void printSessionDuration(long durns)
	{
		final long dursec = TimeUtils.convert(durns, TimeUnit.NANOSECONDS, TimeUnit.SECONDS);
		if (dursec < 60L)
			System.out.println("Session-Duration: " + dursec + " sec");
		else {
			final long mins = dursec / 60L;
			final long secs = dursec - (mins * 60L);
			System.out.println("Session-Duration: " + mins + " min, " + secs + " sec");
		}
	}
	
	private static void printTraceStats(TraceBlockReader block) throws IOException
	{
		// Construct the predefined counters
		final Counter numEntriesCounter = new Counter("Number of Trace-Entries");
		final Counter inputEventCounter = new Counter("Input-Events", numEntriesCounter);
		final Counter mouseEventCounter = new Counter("Mouse-Events", inputEventCounter);
		final Counter keyEventCounter = new Counter("Key-Events", inputEventCounter);
		final Counter serverUpdateCounter = new Counter("Server-Updates", numEntriesCounter);
		final Counter vsyncEntryCounter = new Counter("Visual-Syncs", numEntriesCounter);
		final Counter specialEntryCounter = new Counter("Special-Entries", numEntriesCounter);
		final HashMap<String, Counter> counters = new LinkedHashMap<String, Counter>();
		counters.put(OpCode.MOUSE, mouseEventCounter);
		counters.put(OpCode.KEY, keyEventCounter);
		counters.put(ExtOpCode.VSYNC, vsyncEntryCounter);
		counters.put(ExtOpCode.SCREEN_UPDATE, serverUpdateCounter);
		
		final InstructionParser parser = new InstructionParser();
		final Message msg = new Message();
		String lastOpcode = "";
		Counter counter = null;
		long firstTimestamp = -1L;
		long lastTimestamp = -1L;
		
		// Read trace-block and collect stats
		while (block.read(msg)) {
			parser.setInput(msg.getDataArray(), msg.getOffset(), msg.getLength());
			final String opcode = parser.parseOpcode();
			if (!opcode.contentEquals(lastOpcode)) {
				lastOpcode = opcode;
				counter = counters.get(opcode);
				if (counter == null)
					counter = specialEntryCounter;
			}
			
			counter.increment();
			
			// Update timestamps
			lastTimestamp = msg.getTimestamp();
			if (firstTimestamp < 0)
				firstTimestamp = msg.getTimestamp();
		}
		
		TraceStats.printSessionDuration(lastTimestamp - firstTimestamp);
		
		// Print the counters
		System.out.println(numEntriesCounter.toString());
		System.out.println(INDENT_1X + serverUpdateCounter.toString());
		System.out.println(INDENT_1X + vsyncEntryCounter.toString());
		System.out.println(INDENT_1X + inputEventCounter.toString());
		System.out.println(INDENT_2X + mouseEventCounter.toString());
		System.out.println(INDENT_2X + keyEventCounter.toString());
		if (specialEntryCounter.value() > 0)
			System.out.println(INDENT_1X + specialEntryCounter.toString());
	}
	
	
	private static class Counter
	{
		private final String name;
		private final Counter group;
		private int value;
		
		public Counter(String name)
		{
			this(name, null);
		}
		
		public Counter(String name, Counter group)
		{
			this.name = name;
			this.group = group;
			this.value = 0;
		}
		
		public void increment()
		{
			if (group != null)
				group.increment();
			
			++value;
		}
		
		public int value()
		{
			return value;
		}
		
		public String name()
		{
			return name;
		}
		
		public String toString()
		{
			return (this.name() + ": " + this.value());
		}
	}
}
