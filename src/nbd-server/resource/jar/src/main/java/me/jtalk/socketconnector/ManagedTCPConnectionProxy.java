/*
 * Copyright (C) 2015 Jtalk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.jtalk.socketconnector;


import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;


import me.jtalk.socketconnector.api.TCPConnection;
import me.jtalk.socketconnector.utils.ConnectionRequestInfoUtils;
import me.jtalk.socketconnector.utils.ConnectorLogger;
import me.jtalk.socketconnector.utils.NamedIdObject;

import static me.jtalk.socketconnector.utils.LazyLogger.*;

public class ManagedTCPConnectionProxy implements ManagedConnection, NamedIdObject {

    private long id = 0;

    private long clientId = 0;

    private boolean listening = false;

    private final SocketResourceAdapter adapter;

    private final AtomicReference<TCPConnectionImpl> connection = new AtomicReference<>(null);
    private final EventListeners eventListeners = new EventListeners();
    private final String className = this.getName();

    private final ConnectorLogger log = new ConnectorLogger(className);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public ManagedTCPConnectionProxy(SocketResourceAdapter adapter, NewTCPConnectionRequest info) throws ResourceException {
        this.adapter = adapter;
        reset(info);
    }

    public ManagedTCPConnectionProxy(SocketResourceAdapter adapter, ExistingTCPConnectionRequest info) throws ResourceException {
        this.adapter = adapter;
        reset(info);
    }

    public void disconnect() throws ResourceException {
        lazyTrace(log, "Connection disconnect requested for {}", className);
        adapter.closeTCPConnection(clientId, id);
    }

    public void send(ByteBuffer data) throws ResourceException {
        if (listening) {
            throw new NotSupportedException("Sending data through listening socket");
        } else {
            adapter.sendTCP(clientId, id, data);
        }
    }

    @Override
    public TCPConnection getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        if (!isRunning.get()) {
            throw new ResourceException("Socket connection requested from disconnected managed connection");
        }
        ConnectionRequestInfoUtils.checkInfo(clientId, id, cxRequestInfo);
        TCPConnectionImpl conn = new TCPConnectionImpl(this);
        replaceActiveConnection(conn);
        return conn;
    }

    @Override
    public void destroy() throws ResourceException {
        lazyTrace(log, "Connection destroyal requested for {}", className);
        disconnect();
    }

    @Override
    public void cleanup() throws ResourceException {
        lazyTrace(log, "Connection cleanup requested for {}", className);
        replaceActiveConnection(null);
        isRunning.set(false);
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        lazyTrace(log, "Connection association replacement requested for {}", className);
        if (!(connection instanceof TCPConnectionImpl)) {
            throw new ResourceException("Connection supplied is not a TCPConnectionImpl");
        }
        TCPConnectionImpl newConnection = (TCPConnectionImpl) connection;
        newConnection.reassign(this);
        replaceActiveConnection(newConnection);
        lazyTrace(log, "Connection association replaced for {}", className);
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        lazyTrace(log, "Event listener ''{}'' added for {}", listener, className);
        eventListeners.add(listener);
    }


    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        lazyTrace(log, "Event listener ''{}'' removed for {}", listener, className);
        eventListeners.remove(listener);
    }


    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException("Transactions are not supported");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException("Transactions are not supported");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        throw new NotSupportedException("Connection metadata is not supported");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        lazyTrace(log, "Print writer is set for {}", className);
//        logWriter.setLogWriter(out);
    }


    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}: [id=''{1}'', clientId=''{2}'', listening=''{3}'']",
                super.toString(), getId(), getClientId(), isListening());
    }

    public void requestCleanup() throws ResourceException {
        lazyTrace(log, "Connection cleanup requested for {}", className);
        TCPConnectionImpl conn = connection.get();
        if (conn == null) {
            log.warn("Cleanup requested without connection associated for {}", getName());
            return;
        }
        final ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(conn);
        eventListeners.notifyEvent(event, ConnectionEventListener::connectionClosed);
    }

    public void reset(NewTCPConnectionRequest request) throws ResourceException {
        lazyTrace(log, "Resetting managed connection proxy {} for a new connection: {}", className, request);
//        ValidationUtils.validateInfo(adapter.getValidator(), logWriter::printLog, request);
        clientId = request.getUid();
        listening = request.isListening();
        if (request.isListening()) {
            id = adapter.listenTCP(clientId, request.createInetAddress());
        } else {
            id = adapter.createTCPConnection(clientId, request.createInetAddress());
        }
        if (isRunning.getAndSet(true)) {
            log.error("Managed connection {}: reset while being run", getName());
        }
    }



    public void reset(ExistingTCPConnectionRequest request) throws ResourceException {
        lazyTrace(log, "Resetting managed connection proxy {} for an existing connection: {}", className, request);
        clientId = request.getUid();
        id = request.getId();
        listening = false;
        try {
            adapter.isTCPListener(request.getUid(), request.getId());
        } catch (ResourceException e) {
            lazyTrace(log, "Exception in adapter.isTCPListener invocation for {}", className, e.getMessage());
            // Suppressed
        }
        if (isRunning.getAndSet(true)) {
            log.error("Managed connection {}: reset while being run", getName());
        }
    }



    protected void replaceActiveConnection(TCPConnectionImpl newConnection) {
        lazyTrace(log, "Connection replacing requested for {}", className);
        TCPConnectionImpl old = connection.getAndSet(newConnection);
        if (old != null) {
            lazyTrace(log, "Connection replace succeded for {}: old connection {} will be invalidated", className, old.getName());
            old.invalidate();
        }
    }

    public long getId() {
        return id;
    }

    public long getClientId() {
        return clientId;
    }

    public boolean isListening() {
        return listening;
    }

    public SocketResourceAdapter getAdapter() {
        return adapter;
    }




}
