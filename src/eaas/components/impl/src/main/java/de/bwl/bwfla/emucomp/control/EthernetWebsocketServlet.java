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

package de.bwl.bwfla.emucomp.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;

@ServerEndpoint("/components/{componentId}/ws+ethernet/{hwAddress}")
public class EthernetWebsocketServlet {
    private static final int BUFFER_SIZE = 1500; // this is the MTU of ethernet

    Logger LOG = Logger.getLogger(this.getClass().getName());

    @Inject
    protected NodeManager nodeManager;

    private EthernetConnector connector;
    private Thread readThread;

    @OnOpen
    public void open(Session session, EndpointConfig conf,
            @PathParam("componentId") String componentId,
            @PathParam("hwAddress") String hwAddress) {
        try {
            AbstractEaasComponent component = nodeManager
                    .getComponentById(componentId, AbstractEaasComponent.class);

            IConnector connector = component.getControlConnector(
                    EthernetConnector.getProtocolForHwaddress(hwAddress));

            if (connector == null
                    || !(connector instanceof EthernetConnector)) {
                session.close();
            }
            this.connector = (EthernetConnector) connector;

            ManagedThreadFactory threadFactory = InitialContext
                    .doLookup("java:jboss/ee/concurrency/factory/default");
            this.readThread = threadFactory.newThread(() -> {
                try {
                    InputStream in = this.connector.getInputStream();

                    byte buf[] = new byte[BUFFER_SIZE];
                    int n = 0;
                    while (((n = in.read(buf)) != -1)
                            && !Thread.currentThread().isInterrupted()) {
                        session.getBasicRemote()
                                .sendBinary(ByteBuffer.wrap(buf, 0, n));
                    }

                    session.close(new CloseReason(CloseCodes.GOING_AWAY,
                                "The associated ethernet connection has closed"));

                } catch (InterruptedIOException ignore) {
                    // all is going well, we terminated the thread ourselves
                } catch (IOException _e) {
                    LOG.log(Level.SEVERE, _e.getMessage(), _e);
                } finally {
                    try {
                        session.close();
                    } catch (IOException e1) {
                        LOG.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                }
            });

            readThread.start();

        } catch (IOException | NamingException | BWFLAException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @OnMessage
    public void message(Session session, byte[] msg) {
        try {
            this.connector.getOutputStream().write(msg);
            this.connector.getOutputStream().flush();
        } catch (IOException e) {
            try {
                session.close();
            } catch (IOException e1) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @OnClose
    public void close(Session session, CloseReason reason) {
        readThread.interrupt();
        this.connector.close();
    }
}
