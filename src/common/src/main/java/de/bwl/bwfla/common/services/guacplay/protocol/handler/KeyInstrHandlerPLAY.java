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

package de.bwl.bwfla.common.services.guacplay.protocol.handler;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.KeyState;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.KeyCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.events.EventSink;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;


/**
 * Handler for Guacamole's <i>key-</i> instruction.
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#key-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class KeyInstrHandlerPLAY extends InstructionHandler
{
	private final ICharArrayConsumer output;
	private final EventSink esink;
	private final GuacEvent vsevent;
	
	
	/** Constructor */
	public KeyInstrHandlerPLAY(ICharArrayConsumer output, EventSink esink)
	{
		super(OpCode.KEY);
		this.output = output;
		this.esink = esink;
		this.vsevent = new GuacEvent(EventType.VSYNC_END, this);
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		// Simply pass it as-is to the output!
		final char[] data = instruction.array();
		final int offset = instruction.offset();
		final int length = instruction.length();
		output.consume(data, offset, length);

		// Enter/return key pressed?
		final int key = instruction.argAsInt(0);
		final int pressed = instruction.argAsInt(1);
		if ((key == KeyCode.RETURN) && (pressed == KeyState.PRESSED))
			esink.consume(vsevent);  // End of vsync-processing
	}
}
