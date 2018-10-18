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
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.Nic;



/**
 * 
 * @author Johann Latocha <johann.latocha@rz.uni-freiburg.de>
 * 
 */
public class DosBoxBean extends EmulatorBean {

    @Inject
    @Config("components.binary.dosbox")
    private String dosBoxBean;
    
	private char letter = 'c';
	private String bootArgument = null;
	private final char[] letters = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };


	@Override
	protected String getEmuContainerName(MachineConfiguration env)
	{
		return "dosbox";
	}

	@Override
	protected void setRuntimeConfiguration(MachineConfiguration environment)
			throws BWFLAException {
		super.setRuntimeConfiguration(environment);

		String config = this.getNativeConfig();
		if (config != null && !config.isEmpty()) {
			String[] tokens = config.trim().split(";");
			for (String token : tokens) {
				if (token.isEmpty())
					continue;
				String[] command = token.split(",");
				emuRunner.addArguments(command[0], isXpraBackendEnabled() ? addQuotes(command[1]) : command[1]);
			}
		}
	}

	@Override
	public void prepareEmulatorRunner()
	{
		// Initialize the process-runner
		emuRunner.setCommand(dosBoxBean);
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

/*
		if (value.contains("zip")) {

			Path destFolder = null;
			try { destFolder = Files.createTempDirectory(this.tempDir.toPath(), "dosbox-"); }
			catch(Exception e) {e.printStackTrace();}
			LOG.info("creating temp folder " + destFolder.toString());  
			ZipUtils.unzipTo(new File(value), destFolder.toFile());
			value = destFolder.toString();
		}
*/
		String driveArgument = null;
		String letter = this.getDriveLetter(drive);
		switch (drive.getType()) {
		case FLOPPY:
			driveArgument = String.format("imgmount %s %s %s", letter, imagePath.toString(), "-t floppy");
			break;

		case DISK:
			driveArgument =  String.format("imgmount %s %s %s", letter, imagePath.toString(), "-t hdd");
			break;

		case CDROM:
			driveArgument =  String.format("imgmount %s %s %s", letter, imagePath.toString(), "-t iso");
			break;

		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
			return false;
		}

		if(driveArgument == null) {
			LOG.severe("Device '" + drive.getType() + "' not supported yet.");
			return false;
		}

		emuRunner.addArguments("-c", isXpraBackendEnabled() ? addQuotes(driveArgument) : driveArgument);

		if (drive.isBoot()) {
			if(bootArgument != null)
				LOG.warning("Multiple disks were specified as bootable!");
			bootArgument = "boot -L " + letter;
		}

		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean attach) {
		LOG.warning("Hotplug is not supported by this emulator.");
		return false;
	}

	@Override
	public void start() throws BWFLAException
	{
		if(bootArgument != null){
			emuRunner.addArguments("-c", isXpraBackendEnabled() ? addQuotes(bootArgument) : bootArgument);
		}
		super.start();
	}
	

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private String getDriveLetter(Drive drive) {
		char result = '\0';

		switch (drive.getType()) {
		case FLOPPY:
			result = letters[Integer.parseInt(drive.getUnit())];
			break;

		case CDROM:
		case DISK:
			result = letter++; // TODO: check ide, scsi...
			break;

		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
		}

		return Character.toString(result);
	}

	@Override
	protected boolean addNic(Nic nic) {
		LOG.warning("Network connection is currently not implemented.");
		return false;
	}

	private String addQuotes(String s){
		return "\"" + s + "\"";
	}

}
