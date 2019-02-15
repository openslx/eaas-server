package de.bwl.bwfla.emucomp.components.emulators;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.Nic;

import java.nio.file.Paths;

public class LinAppleBean extends EmulatorBean {
    @Override
    protected void prepareEmulatorRunner() throws BWFLAException {
        emuRunner.setCommand("/usr/local/bin/linapple");
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
        return false;
    }

    @Override
    protected boolean connectDrive(Drive drive, boolean attach) {
        return false;
    }

    @Override
    protected boolean addNic(Nic nic) {
        return false;
    }
}
