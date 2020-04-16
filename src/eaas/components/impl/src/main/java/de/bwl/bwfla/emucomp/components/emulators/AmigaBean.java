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
import java.util.logging.Level;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.Nic;


public class AmigaBean extends EmulatorBean {

    short floppyDriveCounter = 0;

    @Override
    protected String getEmuContainerName(MachineConfiguration env)
    {
        return "fs-uae";
    }

    @Override
    public void prepareEmulatorRunner() {


        emuRunner.setCommand("fs-uae");
        emuRunner.addArgument("--cursor=0");
        emuRunner.addArgument("--automatic-input-grab=0");
//        emuRunner.addArgument("--model=A3000");


        //include native config
        String config = this.getNativeConfig();

        if (config != null && !config.isEmpty()) {
            String[] tokens = config.trim().split("\\s+");
            for (String token : tokens) {
                if (token.isEmpty())
                    continue;
                emuRunner.addArgument(token.trim());
            }
        }
    }

    @Override
    public boolean addDrive(Drive drive) {
        if (drive == null || (drive.getData() == null)) {
            LOG.severe("Drive doesn't contain an image, attach canceled.");
            return false;
        }
        Path imagePath = null;
        try {
            imagePath = Paths.get(this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType())));
            //check if this is first image
            if (floppyDriveCounter < 4)
                emuRunner.addArgument("--floppy-drive-" + floppyDriveCounter + "=" + imagePath.toAbsolutePath());
            emuRunner.addArgument("--floppy-image-" + (floppyDriveCounter) + "=" + imagePath.toAbsolutePath());


            ++floppyDriveCounter;
        } catch (Exception e) {
            LOG.warning("Drive doesn't reference a valid binding, attach canceled." + e.getMessage());
            LOG.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        return false;
    }

    @Override
    public boolean connectDrive(Drive drive, boolean attach) throws BWFLAException
    {
        throw this.newNotImplementedException();
    }

    @Override
    protected boolean addNic(Nic nic) throws BWFLAException
    {
        throw this.newNotImplementedException();
    }
}
