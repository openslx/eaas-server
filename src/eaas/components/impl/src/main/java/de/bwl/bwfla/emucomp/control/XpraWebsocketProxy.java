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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import de.bwl.bwfla.emucomp.components.emulators.IpcSocket;
import de.bwl.bwfla.emucomp.control.connectors.XpraConnector;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;


@ServerEndpoint(value = "/components/{componentId}/xpra", subprotocols = {"binary"})
public class XpraWebsocketProxy extends IPCWebsocketProxy
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	private XpraConnector connector;

	@Inject
	private NodeManager nodeManager = null;

	@OnOpen
	public void open(Session session, EndpointConfig config, @PathParam("componentId") String componentId)
	{
		this.componentId = componentId;

		log.info("Setting up websocket proxy for component '" + componentId + "'...");
		try {
			final AbstractEaasComponent component = nodeManager.getComponentById(componentId, AbstractEaasComponent.class);
			this.connector = (XpraConnector) component.getControlConnector(XpraConnector.PROTOCOL);
			this.iosock = IpcSocket.connect(connector.getIoSocketPath().toString(), IpcSocket.Type.STREAM);

			// Start background thread for streaming from io-socket to client
			{
				this.streamer = new OutputStreamer(session, nodeManager.getWorkerThreadFactory());
				streamer.start();

				connector.setDisconnectHandler(() -> {
					try {
						if (!streamer.isRunning())
							return;

						log.info("Stopping websocket streamer for component '" + componentId + "'...");
						streamer.stop();
					}
					catch (Exception error) {
						log.log(Level.WARNING, "Stopping websocket streamer failed!", error);
					}
				});
			}
		}
		catch (Throwable error) {
			log.log(Level.WARNING, "Setting up websocket proxy for component '" + componentId + "' failed!", error);
			this.stop(session);
		}
	}
}
