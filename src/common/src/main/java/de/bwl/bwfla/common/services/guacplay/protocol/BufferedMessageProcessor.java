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

import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;
import de.bwl.bwfla.common.services.guacplay.util.RingBufferSPSC;


/** A {@link MessageProcessor} for buffered messages. */
public class BufferedMessageProcessor extends MessageProcessor
{
	// Member fields
	protected final RingBufferSPSC<Message> messages;
	
	/** Time to wait, when the message-buffer is empty (in ms) */
	public static final long TIMEOUT_ON_EMPTY_BUFFER  = 1000L;
	
	
	/** Constructor */
	public BufferedMessageProcessor(String name, int msgBufferCapacity)
	{
		super(name);
		
		this.messages = BufferedMessageProcessor.newMessageBuffer(msgBufferCapacity);
	}
	
	/**
	 * Post a new message for asynchronous processing.
	 * @param source The source-type of the message.
	 * @param timestamp The timestamp, when the message was recieved.
	 * @param message The message's data.
	 * @return The current number of unprocessed messages.
	 */
	public int postMessage(SourceType source, long timestamp, CharArrayWrapper message)
	{
		return this.postMessage(source, timestamp, message.array(), message.offset(), message.length());
	}
	
	/**
	 * Post a new message for asynchronous processing.
	 * @param source The source-type of the message.
	 * @param timestamp The timestamp, when the message was recieved.
	 * @param data The array containing the message's data.
	 * @return The current number of unprocessed messages.
	 */
	public int postMessage(SourceType source, long timestamp, char[] data)
	{
		return this.postMessage(source, timestamp, data, 0, data.length);
	}
	
	/**
	 * Post a new message for asynchronous processing.
	 * @param source The source-type of the message.
	 * @param timestamp The timestamp, when the message was recieved.
	 * @param data The array containing the message's data.
	 * @param offset The offset of valid data.
	 * @param length The length of valid data.
	 * @return The current number of unprocessed messages.
	 */
	public int postMessage(SourceType source, long timestamp, char[] data, int offset, int length)
	{
		// Put the new message into the buffer
		final Message message = messages.beginBlockingPutOp();
		message.set(source, timestamp, data, offset, length);
		return messages.finishPutOp();
	}

	/** Returns the buffered messages. */
	public RingBufferSPSC<Message> getMessages()
	{
		return messages;
	}
	
	/** Returns true when pending messages exist, else false. */
	public boolean hasPendingMessages()
	{
		return !messages.isEmpty();
	}
	
	
	/* =============== INTERNAL METHODS =============== */
	
	private static final RingBufferSPSC<Message> newMessageBuffer(int capacity)
	{
		Message[] entries = new Message[capacity];
		for (int i = 0; i < capacity; ++i)
			entries[i] = new Message();
		
		return new RingBufferSPSC<Message>(entries);
	}
}
