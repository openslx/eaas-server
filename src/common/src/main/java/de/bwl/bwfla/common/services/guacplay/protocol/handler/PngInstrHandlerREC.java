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

import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionBuilder;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionSink;
import de.bwl.bwfla.common.services.guacplay.util.FlagSet;


/**
 * Handler for Guacamole's <i>png-</i> instruction (Record-Version).
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#png-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class PngInstrHandlerREC extends PngInstrHandler
{
	/* Member fields */
	private final InstructionBuilder ibuilder;
	private final InstructionSink isink;
	private final Instruction updinstr;
	
	
	/** Constructor */
	public PngInstrHandlerREC(InstructionSink sink, OffscreenCanvas canvas)
	{
		super(canvas);
		
		this.ibuilder = new InstructionBuilder(100);
		this.isink = sink;
		this.updinstr = new Instruction(4);
		
		// Mark this instruction-instance as shared!
		final FlagSet flags = updinstr.flags();
		flags.set(Instruction.FLAG_SHARED_INSTANCE);
		flags.set(Instruction.FLAG_SHARED_ARRAYDATA);
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		super.execute(desc, instruction);
		
		// Construct the instruction for screen update
		ibuilder.start(ExtOpCode.SCREEN_UPDATE, updinstr);
		ibuilder.addArgument(xpos);
		ibuilder.addArgument(ypos);
		ibuilder.addArgument(imgsize.getWidth());
		ibuilder.addArgument(imgsize.getHeight());
		ibuilder.finish();
		
		isink.consume(desc, updinstr);
	}
}
