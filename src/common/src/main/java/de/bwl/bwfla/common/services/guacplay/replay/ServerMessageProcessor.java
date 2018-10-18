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

import de.bwl.bwfla.common.services.guacplay.protocol.AsyncWorker;
import de.bwl.bwfla.common.services.guacplay.protocol.BufferedMessageProcessor;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.util.RingBufferSPSC;

// Internal class (package-private)


final class ServerMessageProcessor extends AsyncWorker
{
	// Member fields
	private final BufferedMessageProcessor processor;
	

	/** Constructor */
	public ServerMessageProcessor(BufferedMessageProcessor processor)
	{
		super();
		
		this.processor = processor;
	}
	
	@Override
	protected void execute() throws Exception
	{
		RingBufferSPSC<Message> messages = processor.getMessages();
		Message message = null;
		
		// Process all buffered messages
		while ((message = messages.beginTakeOp()) != null) {
			try {
				processor.process(message);
			}
			finally {
				message.reset();
				messages.finishTakeOp();
			}
		}
	}

	@Override
	protected void finish() throws Exception
	{
		// Do nothing!
	}

	@Override
	protected String getName()
	{
		return processor.getName();
	}
}
