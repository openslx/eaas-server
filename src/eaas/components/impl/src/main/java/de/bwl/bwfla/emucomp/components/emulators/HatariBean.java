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
import java.nio.file.attribute.FileAttribute;
import java.util.logging.Level;

import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.Nic;



public class HatariBean extends EmulatorBean
{	
    @Inject
    @Config("components.binary.hatari")
    public String hatariBean;

	@Override
	protected String getEmuContainerName(MachineConfiguration env)
	{
		return "hatari";
	}

	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		emuRunner.setCommand(hatariBean);
		
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
		
		if (this.isLocalModeEnabled())
			emuRunner.addArgument("--fullscreen");
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

		if(!drive.isBoot())
		{
			switch(drive.getType()) 
			{
				case DISK:
					try
					{
						Path unzippedHddDir = Files.createTempDirectory(this.getDataDir(), "unzipped_container", new FileAttribute<?>[0]);
						Zip32Utils.unzip(imagePath.toFile(), unzippedHddDir.toFile());
						imagePath = unzippedHddDir;
						emuRunner.addArgument("--harddrive");
					}
					catch(IOException|BWFLAException e)
					{
						LOG.log(Level.WARNING, e.getMessage(), e);
					}

					break;
		
				default:
					LOG.severe("device type '" + drive.getType() + "' not supported yet, attach cancelled");
					return false;
			}
		}
		else
			emuRunner.addArgument("--tos");
		
		emuRunner.addArgument(imagePath.toString());
		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean connect) throws BWFLAException
	{
		throw this.newNotSupportedException();
	}

//	@Override
//	synchronized protected VolatileDrive allocateDrive(DriveType type, Drive proto)
//	{
//        VolatileDrive result = new VolatileDrive();
//        result.setType(type);
//        result.setBoot(false);
//        result.setPlugged(true);
//
//        switch(type)
//        {
//	        case DISK:
//	        	for(Drive d: this.emuEnvironment.getDrive()) 
//	                if(d.getType().equals(Drive.DriveType.DISK) && (d.getData() == null || d.getData().isEmpty())) 
//			            return result;
//	            
//	        default:
//	        	LOG.severe("device type '" + type + "' not supported yet, allocation cancelled");
//	        	break;
//        }
//        
//        return null;
//	}

	protected boolean addNic(Nic nic) throws BWFLAException
	{
		throw this.newNotSupportedException();
	}
}