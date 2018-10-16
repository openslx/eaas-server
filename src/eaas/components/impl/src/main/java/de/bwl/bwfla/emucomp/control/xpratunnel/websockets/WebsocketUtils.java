package de.bwl.bwfla.emucomp.control.xpratunnel.websockets;


import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

class WebsocketUtils {
    static void closeSession(Session session) {
        try {
            session.close();
        } catch (IOException e1) {
            Logger.getLogger(WebsocketUtils.class.getName()).log(Level.SEVERE, e1.getMessage(), e1);
        }
    }
    static void closeSessions(Session[] sessions) {
        for (int i = 0; i < sessions.length; i++) {
            closeSession(sessions[i]);
        }
    }

    static void sendBinary(Session session, ByteBuffer msg) throws IOException {
        //final long start = System.currentTimeMillis();

        session.getBasicRemote().sendBinary(msg);

        //final long duration = System.currentTimeMillis() - start;
        //if (duration > 100) {
        //    final Logger log = Logger.getLogger(WebsocketUtils.class.getName());
        //    log.warning("High websocket data processing time detected: " + duration + "ms");
        //}
    }
}
