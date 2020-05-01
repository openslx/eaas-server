package de.bwl.bwfla.emucomp.components.emulators;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.Nic;

public class BrowserBean extends EmulatorBean {

    private DeprecatedProcessRunner proxyRunner;
    private String networkAddress;

    @Override
    protected void prepareEmulatorRunner() throws BWFLAException {
        emuRunner.setCommand("/usr/local/bin/eaas-browser");

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
    }

    @Override
    public void start() throws BWFLAException
    {
        super.start();

        proxyRunner = new DeprecatedProcessRunner();
        proxyRunner.setCommand("sudo");
        proxyRunner.addArgument("runc");
        proxyRunner.addArguments("exec", "--user", getContainerUserId() + ":" + getContainerGroupId());
        proxyRunner.addArgument(getContainerId());
        proxyRunner.addArgument("/libexec/eaas-proxy");
        proxyRunner.addArgument("8090");
        proxyRunner.addArgument(getContainerHostPathReplacer().apply(this.networkAddress));
        proxyRunner.addArgument(NetworkUtils.getRandomHWAddress());
        proxyRunner.addArgument("dhcp");
        proxyRunner.addArgument("socks5");

        proxyRunner.start();
    }

    @Override
    protected String getEmuContainerName(MachineConfiguration env)
    {
        return "browser";
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
        if (nic == null) {
            LOG.warning("NIC is null, attach canceled.");
            return false;
        }

        this.networkAddress = this.getNetworksDir().resolve("nic_" + nic.getHwaddress()).toString();
        return true;
    }

    @Override
    protected void stopInternal()
    {
        if(proxyRunner != null && proxyRunner.isProcessRunning())
            stopProcessRunner(proxyRunner);

        super.stopInternal();
    }
}
