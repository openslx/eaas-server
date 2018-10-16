package de.bwl.bwfla.emucomp.control;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.CloseReason;
import javax.websocket.MessageHandler;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class ResumableSocket {
    public interface MessageHandlerBoth {
        public void onMessage(String message);

        public void onMessage(byte[] message);
    }

    protected MessageHandlerBoth handler;

    public CloseReason terminated;
    protected Session activeSession;
    protected Async remote;

    protected CircularFifoBuffer messageBuffer = new CircularFifoBuffer(100);
    /* DEBUG protected*/ public long lastSentEventId = 0;
    /* DEBUG protected*/ public long lastReceivedEventId = 0;

    public ResumableSocket(MessageHandlerBoth handler) {
        this.handler = handler;
    }

    public void sendText(String message) {
        synchronized (messageBuffer) {
            messageBuffer.add(message);
            lastSentEventId++;
        }
        if (remote != null)
            remote.sendText(message);
    }

    public void sendBinary(ByteBuffer message) {
        synchronized (messageBuffer) {
            messageBuffer.add(message);
            lastSentEventId++;
        }
        if (remote != null)
            remote.sendBinary(message);
    }

    public void close(CloseReason reason) {
        if (activeSession != null)
            try {
                activeSession.close(reason);
            } catch (IOException e) {
            }
        activeSession = null;
        remote = null;
    }

    public void terminate(CloseReason reason) {
        close(reason);
        terminated = reason;
    }

    @OnOpen
    public void attach(Session session) {
        if (terminated != null) {
            try {
                session.close(terminated);
            } catch (IOException e) {
            }
            return;
        }

        activeSession = session;
        Async newRemote = activeSession.getAsyncRemote();

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("lastEventId", lastReceivedEventId);
        json.add("id", lastSentEventId);
        newRemote.sendText(json.build().toString());

        activeSession.addMessageHandler(new MessageHandler.Whole<String>() {
            boolean firstReceived = false;

            @Override
            public void onMessage(String message) {
                if (firstReceived) {
                    lastReceivedEventId++;
                    handler.onMessage(message);
                    return;
                }
                firstReceived = true;
                JsonObject messageJson = Json.createReader(new StringReader(message)).readObject();
                synchronized (messageBuffer) {
                    if (!resendBuffer(newRemote, messageJson.getInt("lastEventId", Integer.MAX_VALUE))) {
                        try {
                            session.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(4999), "lastEventId not in buffer"));
                        } catch (Exception e) {}
                        return;
                    }
                    if (activeSession == session) {
                        remote = newRemote;
                    } else {
                        // TODO: Or allow two active (sending) sessions?
                        // TODO: Which CloseCode?
                        try {
                            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY,
                                    "only one active conncetion"));
                        } catch (Exception e) {
                        }
                    }
                }
            }
        });
        activeSession.addMessageHandler(new MessageHandler.Whole<byte[]>() {
            @Override
            public void onMessage(byte[] message) {
                lastReceivedEventId++;
                handler.onMessage(message);
            }
        });
    }

    public boolean resendBuffer(Async remote, long lastEventId) {
        synchronized (messageBuffer) {
            long currentId = lastSentEventId - messageBuffer.size();
            if (lastEventId < currentId)
                return false;
            for (Object message : messageBuffer) {
                if (currentId < lastEventId)
                    continue;
                else if (message instanceof String)
                    remote.sendText((String) message);
                else
                    remote.sendBinary((ByteBuffer) message);
            }
        }
        return true;
    }
}
