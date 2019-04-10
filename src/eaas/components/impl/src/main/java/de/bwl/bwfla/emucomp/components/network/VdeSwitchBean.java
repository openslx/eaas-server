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
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.common.utils.WebsocketClient;


// TODO: currently the default of 32 ports is used on the switch,
//       evaluate penalty of higher number and set to e.g. 1024 or use dynamic
//       port allocation
public class VdeSwitchBean extends NetworkSwitchBean {
    @Inject
    @Config("components.binary.vdeswitch")
    private String vdeswitchBinary;

    @Inject
    @Config("components.binary.vdeplug")
    private String vdeplugBinary;

    private ManagedThreadFactory threadFactory;

    // vde_switch process maintenance members
    protected final ProcessRunner runner = new ProcessRunner();
    protected final Map<String, Thread> connections = new HashMap<String, Thread>();

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
            runner.start();
        } catch (IndexOutOfBoundsException | IOException e) {
            throw new BWFLAException("Could not create a vde-switch instance!");
        }
    }

    @Override
    public void destroy() {
        System.out.println("vdeswitch destroyed");
        runner.close();
        final Collection<Thread> threads = this.connections.values();
        threads.forEach((thread) -> thread.interrupt());
        threads.forEach((thread) -> {
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

        LOG.warning("connect to " + ethUrl);

        try {
            // start a new VDE plug instance that connects to the switch
            ProcessRunner runner = new ProcessRunner(this.vdeplugBinary, "-s",
                    this.switchPath);
            runner.start();

            // start a new connection thread
            // it will connect to the websocket url and start forwarding
            // traffic to/from the given process
            Thread readThread = threadFactory
                    .newThread(new Connection(runner, URI.create(ethUrl)));
            readThread.start();
            this.connections.put(ethUrl, readThread);
        } catch (IOException | DeploymentException e) {
            throw new BWFLAException(
                    "Could not establish ethernet connection to " + ethUrl
                            + ": " + e.getMessage(),
                    e);
        }
    }

    @Override
    public URI connect() {  // leaking the connector
        IConnector connector = new EthernetConnector(NetworkUtils.getRandomHWAddress(), this.switchPath);
        addControlConnector(connector);
        return connector.getControlPath(getComponentResource());
    }

    @Override
    public void disconnect(String ethUrl) throws BWFLAException {
        LOG.severe("disconnect " + ethUrl);
        final Thread thread = this.connections.remove(ethUrl);
        if (thread == null)
            throw new BWFLAException("Unknown connection URL: " + ethUrl);

        try {
            // Stop the WebSocket thread!
            thread.interrupt();
            thread.join();
        }
        catch (Throwable error) {
            throw new BWFLAException("Disconnecting '" + ethUrl + "' failed!", error);
        }
    }

    private static class Connection implements Runnable {
        public final ProcessRunner runner;
        public final WebsocketClient wsClient;
        private final URI ethUrl;

        public Connection(final ProcessRunner runner, final URI ethUrl)
                throws DeploymentException, IOException {
            super();
            this.runner = runner;
            this.ethUrl = ethUrl;
            
            // this will immediately establish the connection (or fail with an
            // exception)
            this.wsClient = new WebsocketClient(ethUrl) {
                private final OutputStream stream = runner.getOutputStream();

                @Override
                public void doOnMessage(ByteBuffer msg) {
                    try {

                        final int size = msg.remaining();
                        final byte[] buffer  = new byte[size];
                        msg.get(buffer);
                        stream.write(buffer, 0, size);
                        stream.flush();
                    } catch (Throwable e) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            };

            // if the socket is closed, closing the process/runner will
            // automatically close the associated InputStream and thus
            // terminate this thread
            wsClient.addCloseListener((Session session, CloseReason reason) -> {
                runner.close();
            });
        }

        @Override
        public void run() {
            try {
                InputStream in = this.runner.getInputStream();

                byte buf[] = new byte[1500];
                int n = 0;
                Thread.sleep(1000);
                while (!Thread.currentThread().isInterrupted() && ((n = in.read(buf)) != -1)) {
                    wsClient.send(ByteBuffer.wrap(buf, 0, n));
                }

            } catch (InterruptedIOException ignore) {
                // all is going well, we terminated the thread ourselves
            } catch (Exception e) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
            finally {
                try {
                    System.out.println(" stream has closed " + ethUrl + " -- " + runner.getCommandString()); //  + " " + runner.exitValue());
                    wsClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
