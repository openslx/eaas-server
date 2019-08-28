package de.bwl.bwfla.emucomp.control;

import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emucomp.components.emulators.IpcSocket;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.websocket.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class IPCWebsocketProxy {

    final static Logger log = Logger.getLogger(IPCWebsocketProxy.class.getName());
    protected IpcSocket iosock;
    protected OutputStreamer streamer;
    protected String componentId;

    public static void wait(Path path) throws BWFLAException
    {
        log.info("Waiting for socket to become ready...");

        final int timeout = 60000;  // in ms
        final int waittime = 1000;  // in ms
        for (int numretries = timeout / waittime; numretries > 0; --numretries) {

            DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
            runner.setCommand("awk");
            runner.addArgument("BEGIN {e=1} $8==sock && $4==\"00010000\" {e=0; exit} END {exit e}");
            runner.addArgument("sock=" + path.toString());
            runner.addArgument("/proc/net/unix");
            runner.execute(false, false);
            int code = runner.getReturnCode();
            if (code == 0) {
                log.info("socket seems to be ready now");
                runner.cleanup();
                return;
            }
            runner.cleanup();
            try {
                Thread.sleep(waittime);
            }
            catch (Exception error) {
                // Ignore it!
            }
        }

        throw new BWFLAException("Socket is not available!");
    }

    protected void stop(Session session)
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

    protected class OutputStreamer implements Runnable
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
