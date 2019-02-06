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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import de.bwl.bwfla.emucomp.components.emulators.IpcSocket;
import de.bwl.bwfla.emucomp.control.connectors.XpraConnector;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;


@ServerEndpoint(value = "/components/{componentId}/xpra", subprotocols = {"binary"})
public class XpraWebsocketProxy
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	private String componentId;
	private IpcSocket iosock;
	private OutputStreamer streamer;

	@Inject
	private NodeManager nodeManager = null;


	@OnOpen
	public void open(Session session, EndpointConfig config, @PathParam("componentId") String componentId)
	{
		this.componentId = componentId;

		log.info("Setting up websocket proxy for component '" + componentId + "'...");
		try {
			final AbstractEaasComponent component = nodeManager.getComponentById(componentId, AbstractEaasComponent.class);
			final XpraConnector connector = (XpraConnector) component.getControlConnector(XpraConnector.PROTOCOL);
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
						throw new RuntimeException("Stopping websocket streamer failed!", error);
					}
				});
			}
		}
		catch (Throwable error) {
			log.log(Level.WARNING, "Setting up websocket proxy for component '" + componentId + "' failed!", error);
			this.stop(session);
		}
	}

	@OnMessage
	public void message(Session session, byte[] message, boolean last) throws IOException
	{
		// Forward message from client to iosocket
		iosock.send(message, true);
	}

	@OnClose
	public void close(Session session, CloseReason closeReason)
	{
		final String reason = closeReason.getCloseCode().toString();
		log.info("Websocket session for component '" + componentId + "' closed. Reason: " + reason);
		this.stop(session);
	}

	@OnError
	public void error(Session session, Throwable error)
	{
		log.log(Level.WARNING, "Websocket session for component '" + componentId + "' failed! ", error);
		this.stop(session);
	}

	private void stop(Session session)
	{
		log.info("Stopping websocket proxy for component '" + componentId + "'...");

		if (streamer != null && streamer.isRunning()) {
			try {
				streamer.stop();
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Stopping output-streamer failed!", error);
			}
		}

		if (iosock != null) {
			try {
				iosock.close();
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Closing io-socket failed!", error);
			}
		}

		try {
			session.close();
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Closing websocket session failed!", error);
		}

		log.info("Websocket proxy for component '" + componentId + "' stopped");
	}

	private class OutputStreamer implements Runnable
	{
		private final Thread worker;
		private final Session session;
		private boolean running;

		public OutputStreamer(Session session, ManagedThreadFactory wfactory)
		{
			this.worker = wfactory.newThread(this);
			this.session = session;
			this.running = false;
		}

		public boolean isRunning()
		{
			return running;
		}

		public void start()
		{
			running = true;
			worker.start();
		}

		public void stop() throws InterruptedException
		{
			running = false;
			worker.join();
		}

		@Override
		public void run()
		{
			try {
				final ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
				while (running && iosock.receive(buffer, true)) {
					if (!session.isOpen())
						break;

					session.getBasicRemote()
							.sendBinary(buffer);
				}

				final String message = "Server requested to closed connection!";
				session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, message));
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Forwarding from io-socket to client failed!", error);
			}
		}
	}
}
