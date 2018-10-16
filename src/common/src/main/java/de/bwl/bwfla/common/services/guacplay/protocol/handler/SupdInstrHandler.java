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

import java.util.concurrent.TimeUnit;

import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.CompositeMode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.LineCapStyle;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.LineJoinStyle;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.events.IGuacEventListener;
import de.bwl.bwfla.common.services.guacplay.graphics.ScreenRegion;
import de.bwl.bwfla.common.services.guacplay.graphics.ScreenRegionList;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionBuilder;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;
import de.bwl.bwfla.common.services.guacplay.util.StopWatch;
import de.bwl.bwfla.common.services.guacplay.util.TimeUtils;


/** Handler for the custom <i>supd-</i> instruction. */
public class SupdInstrHandler extends InstructionHandler implements IGuacEventListener
{
	// Member fields
	private final ICharArrayConsumer client;
	private final ScreenRegionList updates;
	private final ScreenRegion update;
	private final InstructionBuilder ibuilder;
	private final StopWatch stopwatch;
	private long minTimestamp;
	private char[] rectinstr;
	private volatile boolean exitflag;
	
	private static final int MIN_HEIGHT = GuacDefs.VSYNC_RECT_HEIGHT / 2;
	private static final int MIN_WIDTH  = GuacDefs.VSYNC_RECT_WIDTH / 2;
	
	/** Threshold for screen-matching computation. */
	private static final float SCREEN_UPDATE_MATCH_THRESHOLD = 0.6F;
	
	/** Time to wait, when screen-matching fails (in ms). */
	private static final long RETRY_TIMEOUT = 1000L;
	
	/** Time to wait, when repeated screen-matching fails (in ns). */
	private static final long UNMATCHED_TIMEOUT
			= TimeUtils.convert(5L, TimeUnit.SECONDS, TimeUnit.NANOSECONDS);
	
	/** ID of the overlay layer. */
	private static final int OVERLAY_LAYER = 2;
	
	// Create reusable raw-instructions
	private static final char[] INSTR_CLEAR_OVERLAY;
	private static final char[] INSTR_CSTROKE_BLUE;
	private static final char[] INSTR_CSTROKE_YELLOW;
	static {
		final InstructionBuilder ibuilder = new InstructionBuilder(512);
		final int thickness = 4;

		// Construct an instruction for clearing the overlay's content
		ibuilder.start(OpCode.DISPOSE);
		ibuilder.addArgument(OVERLAY_LAYER);
		ibuilder.finish();
		
		INSTR_CLEAR_OVERLAY = ibuilder.toCharArray();
		
		// Construct a cstroke-instruction for blue rectangle
		ibuilder.start(OpCode.CSTROKE);
		ibuilder.addArgument(CompositeMode.SRC_OVER);
		ibuilder.addArgument(OVERLAY_LAYER);
		ibuilder.addArgument(LineCapStyle.ROUND);
		ibuilder.addArgument(LineJoinStyle.ROUND);
		ibuilder.addArgument(1);
		ibuilder.addArgument(0);
		ibuilder.addArgument(0);
		ibuilder.addArgument(255);
		ibuilder.addArgument(255);
		ibuilder.finish();

		INSTR_CSTROKE_BLUE = ibuilder.toCharArray();
		
		// Construct a cstroke-instruction for yellow rectangle
		ibuilder.start(OpCode.CSTROKE);
		ibuilder.addArgument(CompositeMode.SRC_OVER);
		ibuilder.addArgument(OVERLAY_LAYER);
		ibuilder.addArgument(LineCapStyle.ROUND);
		ibuilder.addArgument(LineJoinStyle.ROUND);
		ibuilder.addArgument(thickness);
		ibuilder.addArgument(255);
		ibuilder.addArgument(255);
		ibuilder.addArgument(0);
		ibuilder.addArgument(255);
		ibuilder.finish();

		INSTR_CSTROKE_YELLOW = ibuilder.toCharArray();
	}
	
	
	/** Constructor */
	public SupdInstrHandler(ScreenRegionList updates, ICharArrayConsumer socket)
	{
		super(ExtOpCode.SCREEN_UPDATE);
		
		this.client = socket;
		this.updates = updates;
		this.update = new ScreenRegion();
		this.ibuilder = new InstructionBuilder(512);
		this.stopwatch = new StopWatch();
		this.minTimestamp = 0L;
		this.exitflag = false;
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		// Skip screen-update matching, when requested
		if (desc.getTimestamp() < minTimestamp)
			return;
		
		// Get the arguments
		final int width  = instruction.argAsInt(2);
		final int height = instruction.argAsInt(3);

		// Skip small thin-shaped screen-updates!
		if (width < MIN_WIDTH || height < MIN_HEIGHT)
			return;

		final int xpos = instruction.argAsInt(0);
		final int ypos = instruction.argAsInt(1);
		
		// Send visual feedback, when client connected
		if (client != null) {
			// Mark the screen-area
			ibuilder.start(OpCode.RECT);
			ibuilder.addArgument(OVERLAY_LAYER);
			ibuilder.addArgument(xpos);
			ibuilder.addArgument(ypos);
			ibuilder.addArgument(width);
			ibuilder.addArgument(height);
			ibuilder.finish();

			rectinstr = ibuilder.toCharArray();

			// Send constructed rectangle to client
			client.consume(INSTR_CLEAR_OVERLAY, 0, INSTR_CLEAR_OVERLAY.length);
			client.consume(rectinstr, 0, rectinstr.length);
			client.consume(INSTR_CSTROKE_BLUE, 0, INSTR_CSTROKE_BLUE.length);
		}
		
		synchronized (updates) {
			final ScreenRegion bounds = updates.bounds();
			update.set(xpos, ypos, width, height);
			stopwatch.start();
			
			// Does the expected update match with the already updated regions?
			while (!updates.matches(update, SCREEN_UPDATE_MATCH_THRESHOLD)) {
				// No. Does it at least intersect the updated area?
				if (bounds.intersects(update))
					break;  // Yes, skip this update
				
				// Waited long enough?
				if (stopwatch.time() > UNMATCHED_TIMEOUT)
					return;  // Yes, skip this update
				
				// Termination requested?
				if (exitflag)
					return;  // Yes
				
				updates.wait(RETRY_TIMEOUT);
			}
		}
		
		// Send matched rectangle to client
		if (client != null) {
			client.consume(rectinstr, 0, rectinstr.length);
			client.consume(INSTR_CSTROKE_YELLOW, 0, INSTR_CSTROKE_YELLOW.length);
		}
	}
	
	@Override
	public void onGuacEvent(GuacEvent event)
	{
		final int type = event.getType();
		
		if (type == EventType.TERMINATION)
			exitflag = true;
		
		else if ((type == EventType.TRACE_END) && (client != null)) {
			try {
				// Send an instruction to clear the overlay of this handler.
				client.consume(INSTR_CLEAR_OVERLAY, 0, INSTR_CLEAR_OVERLAY.length);
			}
			catch (Exception exception) {
				// Ignore it!
			}
		}
	}
	
	/** Set timestamp for starting screen-update matching. */
	public void setMinTimestamp(long timestamp)
	{
		this.minTimestamp = timestamp;
	}
}
