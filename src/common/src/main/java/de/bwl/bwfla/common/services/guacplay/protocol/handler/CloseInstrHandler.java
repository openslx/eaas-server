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
 * Handler for Guacamole's <i>close-</i> instruction.
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#close-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class CloseInstrHandler extends DrawingInstrHandler
{
	/** Constructor */
	public CloseInstrHandler(OffscreenCanvas canvas)
	{
		super(OpCode.CLOSE, canvas);
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		final int layer = instruction.argAsInt(0);
		
		synchronized (canvas) {
			canvas.closeCurrentPath(layer);
		}
	}
}
