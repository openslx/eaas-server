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

package de.bwl.bwfla.emucomp.components.network;

import java.util.ArrayList;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emucomp.api.VdeSlirpConfiguration;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;

public class VdeSlirpBean extends EaasComponentBean {
    @Inject
    @Config("components.binary.vdeslirp")
    protected String vdeslirp_bin;
    
    protected DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
    protected ArrayList<DeprecatedProcessRunner> vdeProcesses = new ArrayList<DeprecatedProcessRunner>();

    protected String slirpCommand;
    protected VdeSlirpConfiguration config;
    
    @Override
    public void initialize(ComponentConfiguration compConfig) throws BWFLAException {
        try {
            config = (VdeSlirpConfiguration) compConfig;

            // create a vde_switch in hub mode
            // the switch can later be identified using the NIC's MAC address
            String switchName = "nic_" + config.getHwAddress();

            DeprecatedProcessRunner process = new DeprecatedProcessRunner("vde_switch");
            process.addArgument("-hub");
            process.addArgument("-s");
            process.addArgument(this.getWorkingDir().resolve(switchName).toString());
            if(!process.start())
                throw new BWFLAException("Cannot create vde_switch hub for VdeSlirpBean");
            vdeProcesses.add(process);

            runner.setCommand("/libexec/vde/vde_plug");
            runner.addEnvVariable("LD_LIBRARY_PATH", "/libexec/vde");
            runner.addArguments("-s", this.getWorkingDir().resolve(switchName).toString());
            runner.addArgument("--");

            runner.addArguments("/libexec/vde/slirp-helper", "--fd", "3");

            String network;
            String mask;
            if (config.getNetwork() == null || config.getNetwork().isEmpty()) {
                throw new BWFLAException("network attribute is mandatory");
            }

            if(config.getNetwork().contains("/")) {
                try {
                    SubnetUtils net = new SubnetUtils(config.getNetwork());
                    network = net.getInfo().getAddress();
                    mask = net.getInfo().getNetmask();
                } catch (IllegalArgumentException e) {
                    throw new BWFLAException("failed to extract network info from " + config.getNetwork());
                }
            } else {
                 network = config.getNetwork();
                 mask = config.getNetmask();
            }

            if(!network.endsWith(".0"))
                throw new BWFLAException("invalid network: " + network + " " + ((VdeSlirpConfiguration) compConfig).getNetwork() + " " + ((VdeSlirpConfiguration) compConfig).getNetmask());

            LOG.severe("using " +  network + " " + mask);

            runner.addArgument("--disable-ipv6");
            runner.addArguments("--net", network);
            runner.addArguments("--mask",  mask);


            if (config.getGateway() != null)
                runner.addArguments("--host", config.getGateway());

            if (!config.isDhcpEnabled()) {
//              0.0.0.0 means disable dhcp
                runner.addArguments("--dhcp-start", "0.0.0.0");
            }
            else {
                // we assume the full range from 15.
                String dhcpStart = network.substring(0, network.length() - 2) + "15";
                runner.addArguments("--dhcp-start", dhcpStart);
            }

            if (config.getDnsServer() != null && !config.getDnsServer().isEmpty()) {
                runner.addArguments("--dns", config.getDnsServer());
            }
            else
            {
                // we have to make sure to set a DNS, as the user might choose a network outside of 10.0.2.x (default)
                runner.addArguments("--dns", "1.0.0.1");
            }
            
            if (!runner.start())
                throw new BWFLAException("Cannot start vdeslirp process");
            vdeProcesses.add(runner);
            
            
            this.addControlConnector(new EthernetConnector(config.getHwAddress(), this.getWorkingDir().resolve(switchName)));
        } catch (ClassCastException e) {
            throw new BWFLAException("VdeSlirpBean can only be configured from VdeSlirpNode metadata.", e);
        }
    }
    
    @Override
    public void destroy() {
        for (DeprecatedProcessRunner process : this.vdeProcesses) {
            process.stop();
            process.cleanup();
        }
        super.destroy();
    }


    public static VdeSlirpBean createVdeSlirp(VdeSlirpConfiguration config) throws ClassNotFoundException {
        // XXX: only VDE slirps are supported right now 
        String targetBean = "VdeSlirpBean";
        
        Class<?> beanClass = Class.forName(VdeSlirpBean.class.getPackage().getName() + "." + targetBean);
        return (VdeSlirpBean)CDI.current().select(beanClass).get();
    }

    @Override
    public String getComponentType() throws BWFLAException {
        return "slirp";
    }
    
//    @Override
//    public String getComponentType() {
//        return "vdeslirp";
//    }
}
