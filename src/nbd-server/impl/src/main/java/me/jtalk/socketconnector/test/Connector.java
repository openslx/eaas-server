/*
 * NBD Server
 * Oleg Zharkov
 * Uni Freiburg
 * 2016
 * <p>
 * was built on the top of socketconnector
 * https://bitbucket.org/__jtalk/socketconnector
 */

package me.jtalk.socketconnector.test;


import de.bwl.bwfla.common.interfaces.NBDFileProvider;
import me.jtalk.socketconnector.api.TCPConnection;
import me.jtalk.socketconnector.api.TCPConnectionFactory;
import me.jtalk.socketconnector.api.TCPMessage;
import me.jtalk.socketconnector.test.entity.NBDConnection;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.jtalk.socketconnector.test.protocol.NBDProtocol.*;

@AccessTimeout(value = 20, unit = TimeUnit.MINUTES)
@Singleton
public class Connector {
    private static final Logger log = Logger.getLogger(Connector.class.getName());

    public static final String UUID_STRING = "43760934769043760";
    public static final long UUID = Long.parseLong(UUID_STRING);

    private final AtomicBoolean sent = new AtomicBoolean(false);
    private volatile long clientId = -1;
    private volatile long listeningId = -1;
    private HashMap<Long, NBDConnection> connectionMap = new HashMap<>();
    Collection<NBDFileProvider> connectors;


    @Resource(lookup = "java:/socket/TCP")
    TCPConnectionFactory factory;

    /**
     * This method recursively searches the global JNDI NAMESPACE and
     * looks for JNDI names that indicate beans implementing the given
     * interface. Proxies to these beans are then returned.
     * <p>
     * Note: This operation is rather expensive (dozens of ms, if the
     * interface is implemented very often, even more than a second). The
     * result should be cached.
     *
     * @param iface The interface to look for
     * @return A list of all EJB proxies found that implement iface
     */
    protected <T> Collection<T> findEjbsByInterface(final Class<T> iface) {
        Collection<T> list = new ArrayList<T>();
        try {
            Context ctx = InitialContext.doLookup("java:global/");
            return findEjbsByInterface(ctx, iface, list);
        } catch (NamingException e) {
            log.log(Level.SEVERE, "Could not lookup global JNDI index, no NBD connectors will be used", e);
            return list;
        }
    }

    protected <T> Collection<T> findEjbsByInterface(Context ctx, final Class<T> iface) {
        Collection<T> list = new ArrayList<T>();
        return findEjbsByInterface(ctx, iface, list);
    }

    @SuppressWarnings("unchecked")
    protected <T> Collection<T> findEjbsByInterface(Context ctx, final Class<T> iface,
                                                    Collection<T> collection) {
        try {
            NamingEnumeration<NameClassPair> list = ctx.list("");
            while (list.hasMore()) {
                NameClassPair item = list.next();
                String name = item.getName();
                if (name.endsWith("!" + iface.getName())) {
                    collection.add((T) ctx.lookup(name));
                }
                if (item.getClassName().equals("javax.naming.Context")) {
                    findEjbsByInterface((javax.naming.Context) ctx.lookup(name), iface,
                            collection);
                }
            }
        } catch (NamingException ex) {
            log.log(Level.WARNING, "JNDI failure: ", ex);
        }
        return collection;
    }

    /**
     * Initialize the socket and test it
     * Find all the implementations of FileProvider
     */
    public void sendRecord() {
        if (!this.sent.compareAndSet(false, true)) {
            return;
        }



        connectors = findEjbsByInterface(NBDFileProvider.class);
        if (connectors.size() == 0) {
            log.log(Level.SEVERE, "No NBDFileProvider implementation found!");
        }

        int port = 10809;

        try {
            InetSocketAddress local = new InetSocketAddress("0.0.0.0", port);
            try (TCPConnection listening = factory.listen(UUID, local)) {
                this.listeningId = listening.getId();
                log.log(Level.FINEST, "NBD: Test pilot is ready");
            }

        } catch (ResourceException e) {
            throw new EJBException("Exception on connection creation", e);
        }

    }

    /**
     * Send a chunk of data to the initialized client
     *
     * @param id
     * @throws ResourceException
     * @throws IOException
     */
    private void sendDataBlock(long id) throws ResourceException, IOException {
        ByteBuffer messageBuffer = connectionMap.get(id).getReceivedData();
        if (id == this.clientId) {
            log.info("testing.. " + new String(messageBuffer.array()));
        } else {
            int respondMagic = messageBuffer.getInt();
            if (respondMagic == CLIENT_MAGIC_RESPONSE) {
                short commandFlags = messageBuffer.getShort();
                short type = messageBuffer.getShort();
                long handle = messageBuffer.getLong();
                long offset = messageBuffer.getLong();

                long requestLength = Integer.toUnsignedLong(messageBuffer.getInt());
                ByteBuffer buffer = ByteBuffer.allocate((int) requestLength);
                connectionMap.get(id).getFileChannel().read(buffer, offset);

                try (TCPConnection connection = factory.getConnection(UUID, id)) {
                    connection.send(ByteBuffer.wrap(Utils.toBytes(NBD_REPLY_MAGIC)));
                    connection.send(ByteBuffer.wrap(NBD_OK_BYTES));
                    connection.send(ByteBuffer.wrap(Utils.longToBytes(handle)));
                    connection.send(ByteBuffer.wrap(buffer.array()));
                }
            } else {
                log.log(Level.FINEST, new String(messageBuffer.array()));

            }
        }

    }

    /**
     * Read the fileName and create FileChannel
     *
     * @param id
     * @return
     * @throws ResourceException
     * @throws IOException
     */
    private FileChannel initializeNBDFile(long id) throws ResourceException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        FileChannel fileChannel = null;
        ByteBuffer messageBuffer = connectionMap.get(id).getReceivedData();
        int clientFlags = messageBuffer.getInt();
        long magic = messageBuffer.getLong();
        if (magic == CLIENT_MAGIC) {
            int opt = messageBuffer.getInt();
            long length = Integer.toUnsignedLong(messageBuffer.getInt());
            byte[] bytes = new byte[(int) length];
            if (messageBuffer.array().length >= 17 + (int) length) {
                messageBuffer.get(bytes);
                String exportName = new String(bytes);
                log.log(Level.FINEST, "NBD: " + exportName + "id: " + id);


                File exportFile = resolveRequest(exportName);
                if (exportFile == null) {
                    log.log(Level.SEVERE, "NBD: FileNotFound");
                    disconnectClient(id);

                } else {
                    long size = exportFile.length();
                    try {
                        fileChannel = new FileInputStream(exportFile).getChannel();
                    } catch (FileNotFoundException e) {
                        log.log(Level.SEVERE, "NBD: FileNotFound " + exportName);
                        disconnectClient(id);
                    }
                    try (TCPConnection connection = factory.getConnection(UUID, id)) {
                        connection.send(ByteBuffer.wrap(Utils.longToBytes(size)));
                        connection.send(ByteBuffer.wrap(SERVER_RESPOND_FLAG));
                        connection.send(ByteBuffer.wrap(EMPTY_124));
                    }
                    connectionMap.get(id).setInitialPhase(false);
                }
            }
        }


        return fileChannel;
    }


    /**
     * Method to check incoming message for integrity and merge messages otherwise
     *
     * @param message
     * @throws ResourceException
     * @throws IOException
     */
    public void checkTheMessageIntegrity(TCPMessage message) throws ResourceException, IOException {
        long id = message.getConnectionId();
        byte[] messageData = message.getData();

        if (connectionMap.containsKey(id)) {

            if (connectionMap.get(id).isInitialPhase()) {
                mergeByteBuffers(id, messageData);
                processMessageData(id);
            } else if (messageData.length >= 28) {
                connectionMap.get(id).setReceivedData(ByteBuffer.wrap(messageData));
                processMessageData(id);
            } else {
                mergeByteBuffers(id, messageData);
            }

        } else {
            if (message.getData().length >= 17) {
                connectionMap.put(id, new NBDConnection(ByteBuffer.wrap(messageData)));
                processMessageData(id);
            } else
                connectionMap.put(id, new NBDConnection(ByteBuffer.wrap(messageData)));
        }
    }

    /**
     * Process the message and determine, if it's a initializing handshake or export request
     *
     * @param id
     * @throws ResourceException
     * @throws IOException
     */
    private void processMessageData(long id) throws ResourceException, IOException {
        if (connectionMap.get(id).getFileChannel() != null) {
            sendDataBlock(id);
        } else {
            FileChannel fileChannel = null;
            try {
                fileChannel = initializeNBDFile(id);
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (fileChannel != null) {
                connectionMap.get(id).setFileChannel(fileChannel);
                log.log(Level.FINEST, "NBD:  size:" + connectionMap.get(id).getFileChannel().size() + " id: " + id);
            }
        }
    }

    /**
     * When client disconnects, we close and removed corresponding FileChannel
     *
     * @param id
     * @throws IOException
     */
    public void disconnect(long id) throws IOException {
        if (connectionMap.containsKey(id)) {
            if (connectionMap.get(id).getFileChannel() != null)
                connectionMap.get(id).getFileChannel().close();
            connectionMap.remove(id, connectionMap.get(id));
            log.log(Level.FINEST, "NBD: closed and removed " + id);
        }
    }

    /**
     * find an appropriate FIle to export
     *
     * @param exportName
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private File resolveRequest(String exportName) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        for (NBDFileProvider connector : connectors) {
            File file = connector.resolveRequest(exportName);
            if (file != null)
                return file;
        }
        return null;
    }

    /**
     * Merge two messages, in case of message division
     *
     * @param id
     * @param b
     */
    private void mergeByteBuffers(long id, byte[] b) {
        byte[] a = connectionMap.get(id).getReceivedData().array();
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        connectionMap.get(id).setReceivedData(ByteBuffer.wrap(c));
    }

    /**
     * Disconnects the client
     *
     * @param id
     * @throws ResourceException
     */
    private void disconnectClient(long id) throws ResourceException {
        try (TCPConnection connection = factory.getConnection(UUID, id)) {
            connection.disconnect();
        }
    }

}
