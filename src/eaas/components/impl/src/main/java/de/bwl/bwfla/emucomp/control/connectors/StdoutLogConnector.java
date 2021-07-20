package de.bwl.bwfla.emucomp.control.connectors;

import de.bwl.bwfla.emucomp.components.Tail;

import java.net.URI;
import java.nio.file.Path;

public class StdoutLogConnector extends LogConnector {

    public final static String PROTOCOL = "stdout";
    public StdoutLogConnector(Path logPath)
    {
        super(logPath);
    }

    @Override
    public URI getControlPath(URI componentResource) {
        return componentResource.resolve(PROTOCOL);
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }
}
