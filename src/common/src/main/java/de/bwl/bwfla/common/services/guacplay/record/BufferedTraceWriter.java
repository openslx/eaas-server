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

package de.bwl.bwfla.common.services.guacplay.record;

import de.bwl.bwfla.common.services.guacplay.protocol.AsyncWorker;
import de.bwl.bwfla.common.services.guacplay.protocol.BufferedMessageProcessor;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.util.RingBufferSPSC;

// Internal class (package-private)


final class BufferedTraceWriter extends AsyncWorker
{
	// Member fields
	private final BufferedMessageProcessor cmprocessor;
	private final BufferedMessageProcessor smprocessor;
	private final String name;
	private long prevts;
	
	/** Constructor */
	public BufferedTraceWriter(String name, BufferedMessageProcessor cmproc, BufferedMessageProcessor smproc)
	{
		super();

		this.cmprocessor = cmproc;
		this.smprocessor = smproc;
		this.name = name;
		this.prevts = 0L;
	}
	
	@Override
	protected String getName()
	{
		return name;
	}
	
	@Override
	protected void execute() throws Exception
	{
		long curts;

		// Process all buffered messages, sorted according to their timestamps

		while (smprocessor.hasPendingMessages() && cmprocessor.hasPendingMessages()) {
	
			// Process client-messages
			curts = BufferedTraceWriter.curts(smprocessor);
			this.process(cmprocessor, curts);
	
			// Process server-messages
			curts = BufferedTraceWriter.curts(cmprocessor);
			this.process(smprocessor, curts);
		}
		
		if (this.isRunning())
			condition.await(BufferedMessageProcessor.TIMEOUT_ON_EMPTY_BUFFER);
	}

	@Override
	protected void finish() throws Exception
	{
		if (Thread.currentThread().isInterrupted())
			return;

		// Process all pending messages,
		// before the final termination
		
		this.execute();

		if (cmprocessor.hasPendingMessages())
			this.process(cmprocessor, Long.MAX_VALUE);

		if (smprocessor.hasPendingMessages())
			this.process(smprocessor, Long.MAX_VALUE);
	}
	
	
	/* =============== INTERNAL METHODS =============== */
	
	private void process(BufferedMessageProcessor processor, long maxts) throws Exception
	{
		RingBufferSPSC<Message> messages = processor.getMessages();
		Message message = null;
		
		// Process all buffered messages, received 
		// before the specified timepoint maxts
		
		while ((message = messages.beginTakeOp()) != null) {
			final long timestamp = message.getTimestamp();
			if (timestamp > maxts)
				break;
			
			try {
				processor.process(message);
			}
			finally {
				message.reset();
				messages.finishTakeOp();
			}
			
			// Safety check.
			{
				// This should never happen!
				if (timestamp < prevts) {
					String string = "Previous: " + prevts + ", Current: " + timestamp;
					throw new IllegalStateException("Invalid timestamp order! " + string);
				}
				
				prevts = timestamp;
			}
		}
	}
	
	private static long curts(BufferedMessageProcessor processor)
	{
		RingBufferSPSC<Message> messages = processor.getMessages();
		Message message = messages.beginTakeOp();
		if (message != null)
			return message.getTimestamp();
		
		return -1L;
	}
}
