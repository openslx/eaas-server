package de.bwl.bwfla.emucomp.xpra;

import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import org.apache.tamaya.ConfigurationProvider;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;


public class XpraUtils
{
    public static boolean startXpraSession(DeprecatedProcessRunner runner, String command, int port, Logger log)
            throws IOException
    {
        final org.apache.tamaya.Configuration config = ConfigurationProvider.getConfiguration();
        final boolean isGpuEnabled = config.get("components.xpra.enable_gpu", Boolean.class);
        runner.setCommand("xpra");
        runner.addArgument("start");
        runner.addArgument(":" + port);
        runner.addArgument("--bind-tcp=localhost:" + port);
        runner.addArgument("--daemon=no");
        runner.addArgument("--html=on");
        runner.addArgument("--start-child=");
        if (isGpuEnabled)
            runner.addArgValue("vglrun ");
        runner.addArgValue(command);
        // temporary hotfix
        runner.addEnvVariable("XDG_RUNTIME_DIR", "/tmp/" + port);
        return runner.start();
    }

    public static boolean startXpraSession(DeprecatedProcessRunner runner, int port, Logger log)
    {
        runner.setCommand("xpra");
        runner.addArgument("start");
        runner.addArgument(":" + port);
        runner.addArgument("--socket-dir=/tmp");
        runner.addArgument("--socket-dirs=/tmp");
        runner.addArgument("--bind-tcp=localhost:" + port);
        runner.addArgument("--daemon=no");
        runner.addArgument("--html=on");
        // temporary hotfix
        runner.addEnvVariable("XDG_RUNTIME_DIR", "/tmp/" + port);
        return runner.start();
    }

    public static boolean waitUntilReady(int port, long timeout) throws IOException
    {
        final long waittime = 1000;  // in ms
        int numretries = (timeout > waittime) ? (int) (timeout / waittime) : 1;

        while (numretries > 0) {
            if (XpraUtils.isReachable("localhost", port))
                return true;

            try {
                Thread.sleep(waittime);
            }
            catch (Exception error) {
                // Ignore it!
            }

            --numretries;
        }

        return false;
    }

    /**
     * Check, if the port is free and available
     *
     * @param port
     * @return
     */
    private static boolean isReachable(String address, int port) throws IOException
    {
        Socket socket = null;
        try {
            socket = new Socket(address, port);
            return true;
        }
        catch (IOException error) {
            // Not reachable!
            return false;
        }
        finally {
            if (socket != null)
                socket.close();
        }
    }
}
