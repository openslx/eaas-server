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
import java.nio.file.Paths;
import java.util.UUID;
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
import de.bwl.bwfla.emucomp.components.emulators.IpcSocket;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;
import de.bwl.bwfla.emucomp.control.connectors.XpraConnector;

@ServerEndpoint("/components/{componentId}/ws+ethernet/{hwAddress}")
public class EthernetWebsocketServlet extends IPCWebsocketProxy{

    @Inject
    protected NodeManager nodeManager;

    private EthernetConnector connector;

    @OnOpen
    public void open(Session session, EndpointConfig conf,
            @PathParam("componentId") String componentId,
            @PathParam("hwAddress") String hwAddress) {

        try {
            final AbstractEaasComponent component = nodeManager
                    .getComponentById(componentId, AbstractEaasComponent.class);

            IConnector connector = component.getControlConnector(
                    EthernetConnector.getProtocolForHwaddress(hwAddress));

            if (connector == null
                    || !(connector instanceof EthernetConnector)) {
                session.close();
            }
            this.connector = (EthernetConnector) connector;
            String id = UUID.randomUUID().toString();
            this.connector.connect(id);
            this.componentId = componentId;
            wait(Paths.get("/tmp/" + id + ".sock"));
            this.iosock = IpcSocket.connect("/tmp/" + id + ".sock", IpcSocket.Type.STREAM);

            // Start background thread for streaming from io-socket to client
            {
                this.streamer = new OutputStreamer(session, nodeManager.getWorkerThreadFactory());
                streamer.start();
            }
        }
        catch (Throwable error) {
            log.log(Level.WARNING, "Setting up websocket proxy for component '" + componentId + "' failed!", error);
            this.stop(session);
        }
    }

}
