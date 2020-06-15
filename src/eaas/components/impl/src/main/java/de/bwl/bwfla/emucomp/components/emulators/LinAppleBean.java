package de.bwl.bwfla.emucomp.components.emulators;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.Nic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class LinAppleBean extends EmulatorBean {
    @Override
    protected void prepareEmulatorRunner() throws BWFLAException {
        emuRunner.setCommand("/linapple-pie/linapple");
        emuRunner.addArgument("-r");
    }

    @Override
    protected String getEmulatorWorkdir()
    {
        return "/usr/local/linapple/";
    }

    @Override
    protected String getEmuContainerName(MachineConfiguration env)
    {
        return "linapple";
    }

    @Override
    protected boolean addDrive(Drive drive) {
        if (drive == null || (drive.getData() == null)) {
            LOG.severe("Drive doesn't contain an image, attach canceled.");
            return false;
        }

        String unit = drive.getUnit();
        if(unit == null || !(unit.equals("1") || unit.equals("2")))
        {
            LOG.severe("unit has to be either 1 or 2");
            return false;
        }

        Path imagePath = null;
        try {
            imagePath = Paths.get(this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType())));
            //check if this is first image
            emuRunner.addArguments("-" + unit, imagePath.toString());
        } catch (Exception e) {
            LOG.warning("Drive doesn't reference a valid binding, attach canceled." + e.getMessage());
            LOG.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        return false;
    }

    @Override
    protected boolean connectDrive(Drive drive, boolean attach) throws BWFLAException {
        throw this.newNotSupportedException();
    }

    @Override
    protected boolean addNic(Nic nic) throws BWFLAException {
        throw this.newNotSupportedException();
    }
}
