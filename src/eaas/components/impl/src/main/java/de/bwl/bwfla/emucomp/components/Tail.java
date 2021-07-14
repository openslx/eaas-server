package de.bwl.bwfla.emucomp.components;

import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;

import java.io.IOException;
import java.io.InputStream;

public class Tail {

    private DeprecatedProcessRunner tailProcess;
    final private String file;

    public Tail(String file)
    {
        this.file = file;

        tailProcess = new DeprecatedProcessRunner("tail");
        tailProcess.addArguments("-f", "-c", "+0");
        tailProcess.addArgument(file);
        tailProcess.start(false);
    }

    public void cleanup()
    {
        this.tailProcess.stop();
        this.tailProcess.cleanup();
    }

    public InputStream getStream() throws IOException {
        return tailProcess.getStdOutStream();
    }
}
