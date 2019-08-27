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

package de.bwl.bwfla.emucomp.control.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;

import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorBean;


public class EthernetConnector implements IConnector {
    public final static String PROTOCOL = "ws+ethernet";

    private final EmulatorBean emubean;
    private final String hwAddress;
    private final Path vdeSocket;
    private DeprecatedProcessRunner runner = null;
    
    public static String getProtocolForHwaddress(final String hwAddress) {
        return EthernetConnector.PROTOCOL + "+" + hwAddress;
    }

    public EthernetConnector(final String hwAddress, final Path vdeSocket) {
        this(hwAddress, vdeSocket, null);
    }

    public EthernetConnector(final String hwAddress, final Path vdeSocket, EmulatorBean emubean) {
        this.emubean = emubean;
        this.hwAddress = hwAddress;
        this.vdeSocket = vdeSocket;
    }

    @Override
    public URI getControlPath(URI componentResource) {
        return URI.create("ws://hostname/" + componentResource.resolve(EthernetConnector.PROTOCOL + "/" + hwAddress));
    }
    
    @Override
    public String getProtocol() {
        return getProtocolForHwaddress(this.hwAddress);
    }

    public synchronized void connect(String id) {
        if (this.runner != null)
            return;

        if (emubean == null) {
            System.out.println("NULL");
        }


        // Start a new VDE plug instance that connects to the emulator's switch
        this.runner = new DeprecatedProcessRunner();
        runner.setCommand("socat");
        runner.addArguments("unix-listen:/tmp/" + id + ".sock");

        String socatExec = "exec:";
        if (emubean != null && emubean.isContainerModeEnabled()) {
            socatExec += "sudo runc exec --user "
                    + emubean.getContainerUserId() + ":" + emubean.getContainerGroupId() + " " + emubean.getContainerId()  + " ";
        }
        socatExec += "vde_plug " +  this.vdeSocket.toString();

        runner.addArgument(socatExec);

        /*
        if (emubean != null && emubean.isContainerModeEnabled()) {
            // Run the process inside of the running emulator-container!
            runner.addArguments("sudo", "runc", "exec");
            runner.addArguments("--user", emubean.getContainerUserId() + ":" + emubean.getContainerGroupId());
            runner.addArguments(emubean.getContainerId());
        }

        runner.addArguments("vde_plug", "-s", this.vdeSocket.toString());

         */
        runner.start();
    }
    
    public void close() {
        this.runner.stop();
    }
}
