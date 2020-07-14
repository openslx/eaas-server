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
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.*;

import de.bwl.bwfla.common.utils.*;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;


// TODO: currently the default of 32 ports is used on the switch,
//       evaluate penalty of higher number and set to e.g. 1024 or use dynamic
//       port allocation
public class VdeSwitchBean extends NetworkSwitchBean {
    @Inject
    @Config("components.binary.vdeswitch")
    private String vdeswitchBinary;

    private ManagedThreadFactory threadFactory;

    // vde_switch process maintenance members
    protected final ProcessRunner runner = new ProcessRunner();
    protected final Map<String, Thread> connections = new ConcurrentHashMap<>();
    private WebSocketContainer container;

    private Path switchPath;

    public void initialize(ComponentConfiguration compConfig) throws BWFLAException {
        try {
            threadFactory = InitialContext
                    .doLookup("java:jboss/ee/concurrency/factory/default");
        } catch (NamingException e) {
            throw new BWFLAException(
                    "Error initializing VDE switch bean managed thread factory: " + e.getMessage(), e);
        }

        this.switchPath = this.getWorkingDir().resolve("sockets");

        // create a new vde switch instance in tmpdir/sockets
        runner.command(this.vdeswitchBinary);
        runner.addArguments("-s", this.switchPath);

        try {
            container = ContainerProvider.getWebSocketContainer();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw new BWFLAException("Failed to obtain a WebSocketContainer instance " + t.getMessage(), t);
        }

        try {
            runner.start();
        } catch (IndexOutOfBoundsException | IOException e) {
            throw new BWFLAException("Could not create a vde-switch instance!");
        }
    }

    public WebSocketContainer getContainer()
    {
        return container;
    }

    @Override
    public void destroy() {
        runner.close();
        connections.forEach((id, thread) -> thread.interrupt());
        connections.forEach((id, thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        });

        super.destroy();
    }

    @Override
    public void connect(String ethUrl) throws BWFLAException {

        LOG.info("connect to " + ethUrl);

        // start a new connection thread
        // it will connect to the websocket url and start forwarding
        // traffic to/from the given process
        Thread readThread = threadFactory
                .newThread(new Connection(this.switchPath.toString(), ethUrl));
        readThread.start();
        this.connections.put(ethUrl, readThread);
    }

    @Override
    public URI connect() {  // leaking the connector
        IConnector connector = new EthernetConnector(NetworkUtils.getRandomHWAddress(), this.switchPath);
        addControlConnector(connector);
        return connector.getControlPath(getComponentResource());
    }

    @Override
    public void disconnect(String ethUrl) throws BWFLAException {
        LOG.info("disconnect ing connection to" + ethUrl);
        final Thread thread = this.connections.remove(ethUrl);
        if (thread == null)
            throw new BWFLAException("Unknown connection URL: " + ethUrl);

        try {
            // Stop the WebSocket thread!
            thread.interrupt();
            // thread.join();
        }
        catch (Throwable error) {
            throw new BWFLAException("Disconnecting '" + ethUrl + "' failed!", error);
        }
    }

    private static class Connection implements Runnable {
        private final String ethUrl;
        private final String socketPath;

        public Connection(final String socketPath, final String ethUrl) {
            super();
            this.ethUrl = ethUrl;
            this.socketPath = socketPath;
        }

        @Override
        public void run() {
            long start, stop;
            int failCounter = 10;

            final Logger log = Logger.getLogger(Connection.class.getName());

            for(;!Thread.currentThread().isInterrupted() && failCounter > 0;) {

                if (!ethUrl.matches("^wss?://[!#-;=?-\\[\\]_a-z~]+$"))
                        throw new IllegalArgumentException("Illegal WebSocket URL");

                final DeprecatedProcessRunner websocat = new DeprecatedProcessRunner("/libexec/websocat");
                websocat.addArguments("--binary", "--exit-on-eof", "--ping-interval=600");
                websocat.addArguments("exec:vde_plug");
                websocat.addArgument(ethUrl);
                websocat.addArguments("--exec-args", socketPath);
                websocat.start();

                try {
                    start = System.currentTimeMillis();
                    while (websocat.isProcessRunning() && !Thread.currentThread().isInterrupted()) {
                        Thread.sleep(1000);
                    }
                    stop = System.currentTimeMillis();

                    if(stop - start < 2 * 1000)
                    {
                        log.warning("websocat is spinning. remote might be gone... ");
                        Thread.sleep(1000);
                        failCounter--;
                    }
                    else failCounter = 10;

                } catch (InterruptedException e) {
                    log.log(Level.INFO, "Connection has been interrupted!", e);
                } finally {
                    websocat.stop();
                    websocat.cleanup();
                }
            }
        }
    }
}
