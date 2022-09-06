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
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.components.emulators.IpcSocket;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;


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

                Logger.getLogger("EthernetWebsocketServlet").log(Level.SEVERE, "NET_DEBUG connector not found " + componentId + " " + hwAddress);
                session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "component is gone"));
            }
            this.connector = (EthernetConnector) connector;
            String id = UUID.randomUUID().toString();
            this.connector.connect(id);
            this.componentId = componentId;
            this.iosock = IpcSocket.connect("/tmp/" + id + ".sock", IpcSocket.Type.STREAM);

            // Start background thread for streaming from io-socket to client
            {
                this.streamer = new OutputStreamer(session, nodeManager.getWorkerThreadFactory());
                streamer.start();

                this.pingSender = new PingSender(session, nodeManager.getWorkerThreadFactory());
                pingSender.start();
            }
        }
        catch (Throwable error) {
            log.log(Level.WARNING, "Setting up websocket proxy for component '" + componentId + "' failed!", error);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "component is gone"));
            } catch (IOException ignore) { }
            this.stop(session);
        }
    }

    @Override
    protected void stop(Session session)
    {
        connector.close();
        super.stop(session);
    }


}
