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

import java.nio.file.Path;
import java.util.logging.Level;

import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.Nic;

/**
 * @author Johann Latocha <johann.latocha@rz.uni-freiburg.de>
 * 
 */
public class MameBean extends EmulatorBean {
    //@Inject
    //@Config
    private String mameBean;
    
	@Override
	public void prepareEmulatorRunner()
	{
		// Initialize the process-runner
		emuRunner.setCommand(mameBean);
		emuRunner.addArguments("-video", "soft", "-window");
		try {
			// FIXME: read from metadata: String handle = properties.getProperty("rompath");
			String handle = null;
			Path rompath = EmulatorUtils.prepareSoftwareCollection(handle, this.getDataDir());
			emuRunner.addArgument("-rompath");
			emuRunner.addArgument(rompath.toString());
		}
		catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		
		String config = this.getNativeConfig();
		if (config != null) {
			String[] tokens = config.trim().split("\\s+");
			for (String token : tokens)
				emuRunner.addArgument(token.trim());
		}
		
		emuRunner.addEnvVariable("SDLMAME_DESKTOPDIM", "800x600");
	}

	@Override
	public boolean addDrive(Drive drive) {
		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean attach) {
		if (!emuRunner.isProcessRunning()) {
			LOG.warning("Hotplug is unavailable because emulator is not running.");
			return false;
		}

		LOG.severe("Hotplug is not implemented yet.");
		return false;

	}

	@Override
	protected boolean addNic(Nic nic) {
		// TODO Auto-generated method stub
		return false;
	}
}
