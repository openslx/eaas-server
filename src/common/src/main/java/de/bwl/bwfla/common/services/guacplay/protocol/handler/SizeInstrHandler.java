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

import java.util.ArrayList;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionParserException;


/**
 * Handler for Guacamole's <i>size-</i> instruction (server-side).
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#size-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class SizeInstrHandler extends InstructionHandler
{
	private ArrayList<ISizeInstrListener> listeners;
	
	
	/** Constructor */
	public SizeInstrHandler()
	{
		this(2);
	}
	
	/** Constructor */
	public SizeInstrHandler(int numListeners)
	{
		super(OpCode.SIZE);
		this.listeners = new ArrayList<ISizeInstrListener>(numListeners);
	}
	
	/** Add a new listener, that will be notified, when the size-instruction is recieved. */
	public void addListener(ISizeInstrListener listener)
	{
		listeners.add(listener);
	}
	
	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws InstructionParserException
	{
		// The Guacamole protocol contains two different size instructions!
		// But we care only for the server-side one!
		if (desc.getSourceType() != SourceType.SERVER)
			return;
		
		// Get the arguments
		final int layer = instruction.argAsInt(0);
		final int width = instruction.argAsInt(1);
		final int height = instruction.argAsInt(2);
		
		// Ignore invalid sizes!
		if (width < 1 || height < 1)
			return;
		
		// Update all registered listeners
		for (ISizeInstrListener listener : listeners)
			listener.resize(layer, width, height);
	}
}
