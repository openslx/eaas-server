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

import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;


/**
 * Handler for Guacamole's <i>curve-</i> instruction.
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#curve-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class CurveInstrHandler extends DrawingInstrHandler
{
	/** Constructor */
	public CurveInstrHandler(OffscreenCanvas canvas)
	{
		super(OpCode.CURVE, canvas);
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instr) throws Exception
	{
		// Parse curve's arguments
		final int layer = instr.argAsInt(0);
		final int cp1x = instr.argAsInt(1);
		final int cp1y = instr.argAsInt(2);
		final int cp2x = instr.argAsInt(3);
		final int cp2y = instr.argAsInt(4);
		final int x = instr.argAsInt(5);
		final int y = instr.argAsInt(6);
		
		synchronized (canvas) {
			canvas.addCurveSubpath(layer, cp1x, cp1y, cp2x, cp2y, x, y);
		}
	}
}
