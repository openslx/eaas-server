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

package de.bwl.bwfla.common.utils;

import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.*;

public class WebsocketClient extends Endpoint {

    Logger log = Logger.getLogger(this.getClass().getName());

    public interface CloseListener extends EventListener {
        void onClose(Session session, CloseReason closeReason);
    }

    public interface ErrorListener extends EventListener {
        void onError(Session session, Throwable thr);
    }

    private Session session;
    private final URI uri;
    private final List<CloseListener> closeListeners = new ArrayList<CloseListener>();
    private final List<ErrorListener> errorListeners = new ArrayList<ErrorListener>();
    private final WebSocketContainer container;

    public WebsocketClient(WebSocketContainer container, URI uri) {
        this.uri = uri;
        this.container = container;
    }

    public void connect() throws BWFLAException{

        try {

            if (session != null)
                throw new BWFLAException("WebsocketClient already connected");

            List<String> subprotocols = new ArrayList<>();
            subprotocols.add("binary");

            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .preferredSubprotocols(subprotocols)
                    .build();

            this.session = container.connectToServer(this, config, uri);
        } catch (Exception e) {
            throw new BWFLAException("WebSocket connection " + uri + " failed " + e.getMessage(), e);
        }
    }

    @Override
    public final void onOpen(Session session, EndpointConfig config) {
        session.addMessageHandler(new MessageHandler.Partial<ByteBuffer>() {
            @Override
            public void onMessage(ByteBuffer bytes, boolean b) {
                WebsocketClient.this.doOnMessage(bytes);
            }
        });

        try {
            session.getBasicRemote().setBatchingAllowed(false);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        doOnOpen(session, config);
    }

    @Override
    public final void onClose(Session session, CloseReason closeReason) {
        for (CloseListener listener : this.closeListeners) {
            listener.onClose(session, closeReason);
        }
    }

    @Override
    public final void onError(Session session, Throwable thr) {
        for (ErrorListener listener : this.errorListeners) {
            listener.onError(session, thr);
        }
    }

    public String send(final ByteBuffer msg) throws IOException {
        this.session.getBasicRemote().sendBinary(msg);
        return session.getId();
    }

    public void close(CloseReason reason) throws IOException {
        this.session.close(reason);
    }

    public void close() throws IOException {
        this.session.close();
    }
    
    public void addCloseListener(CloseListener listener) {
        this.closeListeners.add(listener);
    }
    
    public void addErrorListener(ErrorListener listener) {
        this.errorListeners.add(listener);
    }

    protected void doOnOpen(Session session, EndpointConfig config) {
    }

    protected void doOnMessage(ByteBuffer msg) {
    }
}