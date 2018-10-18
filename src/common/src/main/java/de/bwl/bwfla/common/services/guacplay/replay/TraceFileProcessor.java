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

package de.bwl.bwfla.common.services.guacplay.replay;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.events.EventSink;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.io.TraceBlockReader;
import de.bwl.bwfla.common.services.guacplay.protocol.AsyncWorker;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.protocol.MessageProcessor;
import de.bwl.bwfla.common.services.guacplay.util.StopWatch;

// Internal class (package-private)


final class TraceFileProcessor extends AsyncWorker
{
	// Member fields
	private final MessageProcessor processor;
	private final TraceBlockReader block;
	private final EventSink esink;
	private final Message message;
	private final StopWatch stopwatch;
	private long lastTimestamp;
	private volatile int numEntriesRead;

	
	/** Constructor */
	public TraceFileProcessor(MessageProcessor processor, TraceBlockReader block, EventSink esink)
	{
		super();
		
		this.processor = processor;
		this.block = block;
		this.esink = esink;
		this.message = new Message();
		this.stopwatch = new StopWatch();
		this.lastTimestamp = Long.MAX_VALUE;
		this.numEntriesRead = 0;
	}
	
	/** Returns the number of entries read from the trace-block. */
	public int getNumEntriesRead()
	{
		return numEntriesRead;
	}
	
	
	/* ========== AsyncMessageWorker Implementation ========== */

	@Override
	protected void execute() throws Exception
	{
		// Read and process the next message
		if (block.read(message)) {
			final long timestamp = message.getTimestamp();
			if (timestamp > lastTimestamp) {
				// Compute the delay for the current message/instruction
				final long elapsed = stopwatch.timems();
				final long delay = timestamp - lastTimestamp - elapsed;
				if (delay > 0)
					Thread.sleep(delay);
			}
			
			processor.process(message);
			
			// Update values for the next run
			lastTimestamp = timestamp;
			stopwatch.start();
			++numEntriesRead;
		}
		else {
			// Nothing left!
			log.info("End of trace-block reached, exiting...");
			this.terminate(false);
		}
	}

	@Override
	protected void finish() throws Exception
	{
		esink.consume(new GuacEvent(EventType.TRACE_END, this));
	}

	@Override
	protected String getName()
	{
		return processor.getName();
	}
}
