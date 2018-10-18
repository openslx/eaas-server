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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;
import de.bwl.bwfla.common.utils.ProcessMonitor;


/** Handler for the custom <i>supd-</i> instruction. */
public class ActfinInstrHandler extends InstructionHandler
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(ActfinInstrHandler.class);
	
	private final ProcessMonitor monitor;

	private static final int  MAX_RETRIES_NUMBER = 10;
	private static final long RETRY_TIMEOUT      = 200L;
	
	/** Constructor */
	public ActfinInstrHandler(ProcessMonitor monitor)
	{
		super(ExtOpCode.ACTION_FINISHED);
		this.monitor = monitor;
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instr) throws Exception
	{
		log.info("Waiting for the emulator to become idle...");
		
		int retries = MAX_RETRIES_NUMBER;
		
		while (monitor.update()) {
			String state = monitor.getValue(ProcessMonitorVID.STATE);
			if (state.contentEquals(ProcessMonitor.STATE_SLEEPING)) {
				if (--retries < 0) {
					log.info("Emulator is in idle state. Proceed with next events.");
					break;
				}
			}
			else retries = MAX_RETRIES_NUMBER;
			
			Thread.sleep(RETRY_TIMEOUT);
		}
	}
}
