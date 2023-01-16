import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import de.bwl.bwfla.emucomp.xpra.PulseAudioStreamer;

public class test {
    static {
        var name = org.freedesktop.gstreamer.Gst.class.getName();
        var logger = java.util.logging.Logger.getLogger(name);
        var handler = new java.util.logging.ConsoleHandler();
        logger.addHandler(handler);
        logger.setLevel(java.util.logging.Level.ALL);
        handler.setLevel(java.util.logging.Level.ALL);
    }

    static PulseAudioStreamer streamer;

    public static void main(String[] args) throws Exception {
        streamer = new PulseAudioStreamer("test", Path.of("/"));
        new Thread(() -> {
            try {
                streamer.play();
                Thread.sleep(1000);
                streamer.stop();
                Thread.sleep(1000);
                streamer.close();
                // System.gc();
                System.out.println("end");
                streamer = null;
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }).start();

        for (; streamer != null;) {
            System.out.println(streamer.pollServerControlMessage(1, TimeUnit.SECONDS));
        }
        for (int i = 0; i < 3; i++) {
            Thread.sleep(100);
            System.gc();
        }
        for (;;) {
            Thread.sleep(1000);
        }
    }
}
