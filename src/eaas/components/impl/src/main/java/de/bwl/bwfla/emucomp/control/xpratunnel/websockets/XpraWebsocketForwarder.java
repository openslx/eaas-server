package de.bwl.bwfla.emucomp.control.xpratunnel.websockets;

import de.bwl.bwfla.common.utils.WebsocketClient;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.*;


@ClientEndpoint(subprotocols = {"binary"})
public class XpraWebsocketForwarder extends WebsocketClient {

    private final Logger log;
    private final Session clientToProxySession;
    private Session proxyToServerSession;

    XpraWebsocketForwarder(URI serverUri, Session clientToProxySession) throws DeploymentException, IOException {
        super(serverUri);
        this.log = Logger.getLogger(this.getClass().getName());
        this.clientToProxySession = clientToProxySession;
        this.addErrorListener((Session session, Throwable error) -> {
            log.log(Level.WARNING, "P2S session '" + session.getId() + "' failed! ", error);
            WebsocketUtils.closeSessions(new Session[]{clientToProxySession, session});
        });
        this.addCloseListener((Session session, CloseReason closeReason) -> {
            final String reason = closeReason.getCloseCode().toString();
            log.info("P2S session '" + session.getId() + "' closed. Reason: " + reason);
            WebsocketUtils.closeSession(clientToProxySession);
        });
    }

    @Override
    protected void doOnOpen(Session session, EndpointConfig config) {
        this.proxyToServerSession = session;

        // Log instance member here can still be uninitialized!
        final Logger log = Logger.getLogger(this.getClass().getName());
        log.info("Start forwarding:  server <--(" + proxyToServerSession + ")--> proxy <--(" + clientToProxySession + ")--> client");
    }

    @Override
    protected void doOnMessage(ByteBuffer msg) {
        try {
            if (clientToProxySession.isOpen()) {
                WebsocketUtils.sendBinary(clientToProxySession, msg);
            } else this.close();
        } catch (Throwable error) {
            final String errmsg = "Forwarding data from session '" + proxyToServerSession.getId()
                    + "' to session '" + clientToProxySession.getId() + "' failed!";

            log.log(Level.WARNING, errmsg, error);
            WebsocketUtils.closeSessions(new Session[]{clientToProxySession, proxyToServerSession});
        }
    }

    /* Getters */

    protected Session getClientSession() {
        return clientToProxySession;
    }

    protected Session getServerSession() {
        return proxyToServerSession;
    }
}
