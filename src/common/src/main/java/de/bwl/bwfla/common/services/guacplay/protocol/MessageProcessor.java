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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A class for processing {@link Message}s. */
public class MessageProcessor
{
	// Member fields
	private final InstructionDescription description;
	private final InstructionParser parser;
	private final Instruction instruction;
	private InstructionHandler curHandler;
	private String curOpcode;
	private int msgNumProcessed;
	private int msgNumSkipped;
	
	protected final Map<String, InstructionHandler> handlers;
	protected final String msgProcessorName;
	
	/** Logger instance. */
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	/** Constructor */
	public MessageProcessor(String name)
	{
		this.handlers = new HashMap<String, InstructionHandler>();
		this.description = new InstructionDescription();
		this.parser = new InstructionParser();
		this.instruction = new Instruction(8);
		this.curHandler = null;
		this.curOpcode = null;
		this.msgNumProcessed = 0;
		this.msgNumSkipped = 0;
		this.msgProcessorName = name;
		
		// Mark this instruction-instance as shared!
		instruction.flags().set(Instruction.FLAG_SHARED_INSTANCE);
	}
	
	/** Returns the name of this message-processor. */
	public String getName()
	{
		return msgProcessorName;
	}
	
	/** Reset the counters for processed and skipped messages. */
	public final void resetMessageCounters()
	{
		msgNumProcessed = 0;
		msgNumSkipped = 0;
	}
	
	/** Returns the number of processed messages. */
	public int getNumProcessedMessages()
	{
		return msgNumProcessed;
	}
	
	/** Returns the number of messages, for which no handlers could be found. */
	public int getNumSkippedMessages()
	{
		return msgNumSkipped;
	}
	
	/**
	 * Find and return the registered {@link InstructionHandler} for the specified opcode.
	 * @param opcode The opcode to use for lookup.
	 * @return The handler if one was registered, else null.
	 */
	public final InstructionHandler getInstructionHandler(String opcode)
	{
		return handlers.get(opcode);
	}
	
	/**
	 * Add an {@link InstructionHandler} for the specified opcode.
	 * @param opcode The opcode to use for lookup.
	 * @param handler The new handler instance.
	 * @return The previously registered handler or null.
	 */
	public final InstructionHandler addInstructionHandler(String opcode, InstructionHandler handler)
	{
		return handlers.put(opcode, handler);
	}
	
	/**
	 * Remove the {@link InstructionHandler} for the specified opcode.
	 * @param opcode The opcode to use for lookup.
	 * @return The removed handler if one was registered, else null.
	 */
	public final InstructionHandler removeInstructionHandler(String opcode)
	{
		return handlers.remove(opcode);
	}
	
	/**
	 * Process the specified message using one of the registered handlers.
	 * @param message The message to process.
	 */
	public final void process(Message message) throws Exception
	{
		// NOTE: Guacamole's client and server can send
		//       multiple instructions in a single message!

		description.setSourceType(message.getSourceType());
		description.setTimestamp(message.getTimestamp());

		// Get the message's data
		final char[] data = message.getDataArray();
		final int offset = message.getOffset();
		final int length = message.getLength();
				
		// Handle all recieved instructions
		parser.setInput(data, offset, length);
		while (parser.available()) {
			// Preparse the opcode only!
			String opcode = parser.parseOpcode();
			final char c = opcode.charAt(0);
			if (c >= '0' && c <= '9') {
				// It's a timestamped instruction,
				// use the parsed timestamp then
				long tsvalue = Long.parseLong(opcode);
				description.setTimestamp(tsvalue);
				
				// Parse the actual opcode now
				parser.skip(1);
				opcode = parser.parseOpcode();
			}
			if (opcode != curOpcode) {
				// Different opcode, lookup the handler
				curHandler = handlers.get(opcode);
				curOpcode = opcode;
			}

			// Parse arguments and run the handler
			if (curHandler != null) {
				parser.parseArguments(instruction);
				curHandler.execute(description, instruction);
				++msgNumProcessed;
			}
			else {
				// Skip the whole instruction
				parser.skipArguments();

				// Log it as warning
				String instrmsg = new String(data, parser.getInstrOffset(), parser.getInstrLength());
				log.warn("No handler found in {}, skip:  {}", msgProcessorName, instrmsg);
				++msgNumSkipped;
			}
		}
	}
}
