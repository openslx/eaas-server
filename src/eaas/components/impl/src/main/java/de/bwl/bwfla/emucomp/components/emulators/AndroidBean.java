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

package de.bwl.bwfla.emucomp.components.emulators;

import java.io.File;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;

public class AndroidBean extends EmulatorBean
{
	String emulatorBinary = "/home/bwfla/emulator/run.sh";
	
	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		File exec = new File(emulatorBinary);
		if (exec == null || !exec.exists())
			throw new BWFLAException("EmulatorContainer's executable not found! Make sure you have specified " + "a valid path to your executable in the corresponding 'properties' file");
	
		emuRunner.setCommand(exec.getAbsolutePath());

		emuRunner.setWorkingDirectory(new File("/home/bwfla/emulator").toPath());
		
	}

	@Override
	protected boolean addDrive(Drive drive) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean connectDrive(Drive drive, boolean attach) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean addNic(Nic nic) {
		// TODO Auto-generated method stub
		return false;
	}

	
}