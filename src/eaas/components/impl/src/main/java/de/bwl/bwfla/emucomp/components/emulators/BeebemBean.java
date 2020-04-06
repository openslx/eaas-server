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
import java.util.logging.Level;

import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;
import de.bwl.bwfla.emucomp.api.EmulatorUtils.XmountOutputFormat;



public class BeebemBean extends EmulatorBean
{	
    @Inject
    @Config("components.binary.beebem")
    public String beebemBean;

	@Override
	protected String getEmuContainerName(MachineConfiguration env)
	{
		return "beebem";
	}

	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		final Path discsDir = this.getDataDir().resolve("discs");

		// we mount all bindings now
		try {
			Files.createDirectory(discsDir);
		} catch (IOException e1) {
			LOG.log(Level.SEVERE, e1.getMessage(), e1);
		}
		
		// LOG.info(emuEnvironment.value());
		for(String b : bindings.entries().keySet())
		{
			LOG.info("mounting: " + b);
			try {
				Path imagePath = Paths.get(lookupResource("binding://" + b, XmountOutputFormat.RAW));
				Path link = discsDir.resolve(imagePath.getFileName().toString() + ".ssd");
				Path link2 = discsDir.resolve(imagePath.getFileName().toString() + ".dsd");
				Files.createSymbolicLink(link, imagePath);
				Files.createSymbolicLink(link2, imagePath);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		emuRunner.setCommand(beebemBean);
		emuRunner.setWorkingDirectory(this.getDataDir());
		String config = this.getNativeConfig();
		if (config != null && !config.isEmpty()) {
			String[] tokens = config.trim().split("\\s+");
			for (String token : tokens)
			{
				if(token.isEmpty())
						continue;
				emuRunner.addArgument(token.trim());
			}
		}
		
//		if (this.isLocalModeEnabled()) {
//			emuRunner.addArgument("--fullscreen");
//		}
	}
	
	@Override
	public boolean addDrive(Drive drive)
	{
        if (drive == null || (drive.getData() == null)) {
            LOG.warning("Drive doesn't contain an image, attach canceled.");
            return false;
        }

        Path imagePath = null;
        try {
            imagePath = Paths.get(this.lookupResource(drive.getData(),
                    this.getImageFormatForDriveType(drive.getType())));
        } catch (Exception e) {
            LOG.warning(
                    "Drive doesn't reference a valid binding, attach cancelled.");
            return false;
        }
		if(drive == null || imagePath == null) 
		{
			LOG.warning("drive doesn't contain an image, attach cancelled");
			return false;
		}

		Path link = this.getDataDir().resolve(imagePath.getFileName().toString() + ".ssd");
        try {
			Files.deleteIfExists(link);
			Files.createSymbolicLink(link, imagePath);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
      
        imagePath = link;
		emuRunner.addArgument(imagePath.toString());
		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean connect) throws BWFLAException
	{
		throw this.newNotSupportedException();
	}


	protected boolean addNic(Nic nic) throws BWFLAException
	{
		throw this.newNotSupportedException();
	}
}