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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.common.utils.net.ConfigKey;
import de.bwl.bwfla.common.utils.net.PortRangeProvider.Port;
import de.bwl.bwfla.emucomp.api.VdeSocksConfiguration;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;
import de.bwl.bwfla.emucomp.control.connectors.Socks4Connector;

public class VdeSocksBean extends EaasComponentBean {
    @Inject
    @Config("components.binary.vdesocks")
    protected String vdesocks_bin;
    
    @Inject
    @Config("components.socks.listenaddress")
    protected String listenAddress;
    
    @Inject
    @ConfigKey("components.socks.ports")
    protected Port listenPort;
    
    protected ArrayList<ProcessRunner> vdeProcesses = new ArrayList<ProcessRunner>();
    protected ProcessRunner runner = new ProcessRunner();
    
    protected VdeSocksConfiguration config;
    
    @Override
    public void initialize(ComponentConfiguration compConfig) throws BWFLAException {
        try {
            config = (VdeSocksConfiguration) compConfig;
            
            runner.command(vdesocks_bin);
            
            runner.addArguments("--remotehost", config.getIp4Address(),
                                "--netmask", Integer.toString(config.getNetmask()),
                                "--listen", listenAddress,
                                "--port", listenPort.get());
            
            // create a vde_switch in hub mode
            // the switch can later be identified using the NIC's MAC address
            String switchName = "nic_" + config.getHwAddress();

            ProcessRunner process = new ProcessRunner("vde_switch");
            process.addArguments("-hub");
            process.addArguments("-s");
            process.addArguments(this.getWorkingDir().resolve(switchName));
            process.start();
            vdeProcesses.add(process);
            
            runner.addArguments("-s", this.getWorkingDir().resolve(switchName).toString());
            
            runner.start();
            vdeProcesses.add(runner);
            
            this.addControlConnector(new EthernetConnector(config.getHwAddress(), this.getWorkingDir().resolve(switchName)));
            this.addControlConnector(new Socks4Connector(listenPort.get()));
        } catch (ClassCastException e) {
            throw new BWFLAException("Could not understand VdeSockspBean's config.", e);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (IndexOutOfBoundsException e) {
            // TODO Auto-generated catch block
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        for (ProcessRunner process : this.vdeProcesses) {
            process.stop();
        }
        listenPort.release();
        super.destroy();
    }
    
    public static VdeSocksBean createVdeSocks(VdeSocksConfiguration config) throws ClassNotFoundException {
        // XXX: only VDE SOCKS is supported right now 
        String targetBean = "VdeSocksBean";
        
        Class<?> beanClass = Class.forName(VdeSocksBean.class.getPackage().getName() + "." + targetBean);
        return (VdeSocksBean)CDI.current().select(beanClass).get();
    }
    
    @Override
    public String getComponentType() throws BWFLAException {
        return "socks";
    }
}
