package de.bwl.bwfla.emucomp.control.xpratunnel.websockets;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import de.bwl.bwfla.emucomp.control.connectors.XpraConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;


@ServerEndpoint(value = "/components/{componentId}/xpra/", subprotocols = {"binary"})
public class XpraWebsocketProxy
{
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private XpraWebsocketForwarder forwarder;

    @Inject
    private NodeManager nodeManager;

    @OnOpen
    public void open(Session clientToProxySession, EndpointConfig conf, @PathParam("componentId") String componentId)
    {
        log.info("Setting up websocket proxy for component '" + componentId + "'...");
        try {
            final AbstractEaasComponent component = nodeManager.getComponentById(componentId, AbstractEaasComponent.class);
            final IConnector connector = component.getControlConnector(XpraConnector.PROTOCOL);
            final int port = ((XpraConnector) connector).getPort();
            final URI uri = URI.create("ws://localhost:" + port); // no trailing slash
            log.info("Connecting client websocket session '" + clientToProxySession.getId() + "' to Xpra server at '" + uri.toString() + "'...");
            this.forwarder = new XpraWebsocketForwarder(uri, clientToProxySession);
        }
        catch (Throwable error) {
            log.log(Level.WARNING, "Setting up websocket proxy for component '" + componentId + "' failed!", error);
            WebsocketUtils.closeSession(clientToProxySession);
        }
    }

    @OnMessage
    public void message(Session clientToProxySession, ByteBuffer msg, boolean last) throws BWFLAException, IOException
    {
        forwarder.send(msg);
    }

    @OnClose
    public void close(Session clientToProxySession, CloseReason closeReason)
    {
        final String reason = closeReason.getCloseCode().toString();
        log.info("C2P session '" + clientToProxySession.getId() + "' closed. Reason: " + reason);
        WebsocketUtils.closeSessions(new Session[]{forwarder.getServerSession()});
    }

    @OnError
    public void error(Session clientToProxySession, Throwable error)
    {
        log.log(Level.WARNING, "C2P session '" + clientToProxySession.getId() + "' failed! ", error);
        WebsocketUtils.closeSession(clientToProxySession);
    }
}
