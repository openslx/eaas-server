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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;

/**
 * @author Johann Latocha <johann.latocha@rz.uni-freiburg.de>
 * 
 */
public class MessBean extends EmulatorBean
{
    //@Inject
    //@Config
    private String messBean;
	@Override
	public void prepareEmulatorRunner()
	{
		String config = this.getNativeConfig();
		
		// Initialize the process-runner
		emuRunner.setCommand(messBean);
		emuRunner.addArguments("-video", "soft", "-window");
		if (config != null) {
			String[] tokens = config.trim().split("\\s+");
			for (String token : tokens)
				emuRunner.addArgument(token.trim());
		}
		
		emuRunner.addEnvVariable("SDLMAME_DESKTOPDIM", "800x600");
	}

	@Override
	public boolean addDrive(Drive drive) {
		if (drive == null || (drive.getData() == null)) {
            LOG.warning("Drive doesn't contain an image, attach canceled.");
            return false;
        }
        
        Path imagePath = null;
        try {
            imagePath = Paths.get(this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType())));
        } catch (Exception e) {
            LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
            return false;
        }

		switch (drive.getType()) {
		case FLOPPY:
			emuRunner.addArgument("-flop");
			emuRunner.addArgument(imagePath.getFileName().toString());
			return true;

		case DISK:
			emuRunner.addArgument("-rompath");
			emuRunner.addArgument(this.getDataDir().toString());
			
			final Path filepath = imagePath;
			final String filename = filepath.getFileName().toString();
			final int i = filename.indexOf(".zip");
			emuRunner.addArgument(filename.substring(0, i));

			// create a symbolic link to our ROM file because we have to keep the original filename
			try {
				Path link = this.getDataDir().resolve(filename);
				Files.createSymbolicLink(link, filepath);
			} catch (IOException | UnsupportedOperationException e) {
				LOG.severe(e.getMessage());
			}
			break;

		case CDROM:
			emuRunner.addArgument("-cdrom");
			emuRunner.addArgument(imagePath.getFileName().toString());
			return false;

		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
			return false;
		}

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
		LOG.warning("Network connection is currently not implemented.");
		return false;
	}
}
