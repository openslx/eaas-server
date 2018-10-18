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

import de.bwl.bwfla.common.services.guacplay.GuacDefs.CompositeMode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.LineCapStyle;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.LineJoinStyle;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.events.IGuacEventListener;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.graphics.ScreenRegionList;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionBuilder;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;


/** Handler for Guacamole's <i>png-</i> instruction (Replay-Version). */
public class PngInstrHandlerPLAY extends PngInstrHandler implements IGuacEventListener
{
	// Member fields
	private final ScreenRegionList updates;
	private final ICharArrayConsumer client;
	private final InstructionBuilder ibuilder;
	
	/** ID of the overlay layer. */
	private static final int OVERLAY_LAYER = 1;
	
	// Create reusable raw-instruction
	private static final char[] INSTR_CLEAR_OVERLAY;
	private static final char[] INSTR_CSTROKE_GRAY;
	static {
		final InstructionBuilder ibuilder = new InstructionBuilder(512);
		final int thickness = 1;
		
		// Construct an instruction for clearing the overlay's content
		ibuilder.start(OpCode.DISPOSE);
		ibuilder.addArgument(OVERLAY_LAYER);
		ibuilder.finish();
		
		INSTR_CLEAR_OVERLAY = ibuilder.toCharArray();

		// Construct a cstroke-instruction for gray rectangle
		ibuilder.start(OpCode.CSTROKE);
		ibuilder.addArgument(CompositeMode.SRC_OVER);
		ibuilder.addArgument(OVERLAY_LAYER);
		ibuilder.addArgument(LineCapStyle.SQUARE);
		ibuilder.addArgument(LineJoinStyle.ROUND);
		ibuilder.addArgument(thickness);
		ibuilder.addArgument(220);
		ibuilder.addArgument(220);
		ibuilder.addArgument(220);
		ibuilder.addArgument(255);
		ibuilder.finish();
		
		INSTR_CSTROKE_GRAY = ibuilder.toCharArray();
	}
	

	/** Constructor */
	public PngInstrHandlerPLAY(OffscreenCanvas canvas, ScreenRegionList updates, ICharArrayConsumer socket)
	{
		super(canvas);
		
		this.updates = updates;
		this.client = socket;
		this.ibuilder = new InstructionBuilder(512);
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		super.execute(desc, instruction);
		
		final int width = imgsize.getWidth();
		final int height = imgsize.getHeight();

		// Insert the new update
		synchronized (updates) {
			updates.add(xpos, ypos, width, height);
			updates.notify();
		}

		// Send visual feedback, when client connected
		if (client != null) {
			// Mark the updated screen-area
			ibuilder.start(OpCode.RECT);
			ibuilder.addArgument(OVERLAY_LAYER);
			ibuilder.addArgument(xpos);
			ibuilder.addArgument(ypos);
			ibuilder.addArgument(width);
			ibuilder.addArgument(height);
			ibuilder.finish();

			final char[] rectinstr = ibuilder.toCharArray();
			
			// Send constructed rectangle to client
			client.consume(rectinstr, 0, rectinstr.length);
			client.consume(INSTR_CSTROKE_GRAY, 0, INSTR_CSTROKE_GRAY.length);
		}
	}
	
	@Override
	public void onGuacEvent(GuacEvent event)
	{
		if (event.getType() != EventType.VSYNC_END)
			return;

		// Clear region-list
		synchronized (updates) {
			updates.clear();
		}

		// Clear the overlay
		if (client != null) {
			try {
				client.consume(INSTR_CLEAR_OVERLAY, 0, INSTR_CLEAR_OVERLAY.length);
			}
			catch (Exception exception) {
				// Ignore it!
			}
		}
	}
}
