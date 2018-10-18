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

import de.bwl.bwfla.common.services.guacplay.util.AbstractSink;


/** A collection of listeners/consumers for Guacamole's instructions. */
public class InstructionSink extends AbstractSink<IGuacInstructionConsumer>
{
	/** Constructor */
	public InstructionSink(int capacity)
	{
		super(capacity);
	}

	/** Send the data to all registered listeners/consumers. */
	public void consume(InstructionDescription desc, Instruction instruction) throws Exception
	{
		for (IGuacInstructionConsumer consumer : consumers)
			consumer.consume(desc, instruction);
	}
}
