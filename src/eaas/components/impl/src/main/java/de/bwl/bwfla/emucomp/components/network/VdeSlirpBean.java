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
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
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
            runner.setCommand(vdeslirp_bin);
            
            if (config.getIp4Address() != null && !config.getIp4Address().isEmpty()) {
                runner.addArguments("--host", config.getIp4Address() + "/" + config.getNetmask());
            }
            if (config.isDhcpEnabled()) {
                runner.addArgument("--dhcp");
            }
            if (config.getDnsServer() != null && !config.getDnsServer().isEmpty()) {
                runner.addArguments("--dns", config.getDnsServer());
            }
            
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
            
            runner.addArguments("-s", this.getWorkingDir().resolve(switchName).toString());
            
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
