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

import de.bwl.bwfla.common.services.guacplay.GuacDefs.MouseButton;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionSink;
import de.bwl.bwfla.common.services.guacplay.protocol.VSyncInstrGenerator;
import de.bwl.bwfla.common.services.guacplay.util.FlagSet;


/**
 * Handler for Guacamole's <i>mouse-</i> instruction (Record-Version).
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#mouse-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class MouseInstrHandlerREC extends InstructionHandler
{
	// Member fields
	private final VSyncInstrGenerator vsyncgen;
	private final InstructionSink isink;
	private final Instruction vsync;
	private long lastPressedTimestamp;
	private int lastPressedButtons;
	private int lastButtons;

	/** Mask for mouse buttons only! (excluding scroll-up and scroll-down buttons) */
	private static final int BUTTONS_MASK = MouseButton.LEFT | MouseButton.MIDDLE | MouseButton.RIGHT;
	
	/** Maximal time between two mouse clicks (in ms) */
	private static final long DOUBLE_CLICK_TIMEOUT = 250L;
	
	
	/** Constructor */
	public MouseInstrHandlerREC(InstructionSink isink, VSyncInstrGenerator vsyncgen)
	{
		super(OpCode.MOUSE);
		this.vsyncgen = vsyncgen;
		this.isink = isink;
		this.vsync = new Instruction(5);
		this.lastPressedTimestamp = Long.MIN_VALUE;
		this.lastPressedButtons = 0;
		this.lastButtons = 0;
		
		// Mark this instruction-instance as shared!
		final FlagSet flags = vsync.flags();
		flags.set(Instruction.FLAG_SHARED_INSTANCE);
		flags.set(Instruction.FLAG_SHARED_ARRAYDATA);
	}
	
	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		// Handles the following cases:
		//    1) Single-click
		//    2) Double-click
		//    3) Dragging (mouse down and move)
		
		// Get the mouse-button mask
		final int buttons = instruction.argAsInt(2) & BUTTONS_MASK;

		// Any button pressed?
		if ((buttons != 0) && (buttons != lastButtons)) {
			// Yes! Is it a double click?
			final long timestamp = desc.getTimestamp();
			if ((buttons != lastPressedButtons) || (timestamp - lastPressedTimestamp) > DOUBLE_CLICK_TIMEOUT) {
				// No, generate and send the vsync-instruction to the sink
				final int xpos = instruction.argAsInt(0);
				final int ypos = instruction.argAsInt(1);
				vsyncgen.generate(xpos, ypos, vsync);
				isink.consume(desc, vsync);

				// Update variables for next run
				lastPressedTimestamp = timestamp;
				lastPressedButtons = buttons;
			}
			else {
				// We have a double-click!
				// Reset buttons for next run.
				lastPressedButtons = 0;
			}
		}
		
		lastButtons = buttons;  // Update
		
		// Write the instruction to sink
		isink.consume(desc, instruction);
	}
}
