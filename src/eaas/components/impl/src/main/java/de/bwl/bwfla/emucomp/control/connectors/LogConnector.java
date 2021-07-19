package de.bwl.bwfla.emucomp.control.connectors;

import de.bwl.bwfla.emucomp.components.Tail;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class LogConnector implements IConnector {

    protected List<Tail> logListener = new ArrayList<>();
    private final Path logPath;

    protected LogConnector(Path logPath)
    {
        this.logPath = logPath;
    }

    public void cleanup()
    {
        logListener.forEach(Tail::cleanup);
    }

    public Tail connect()
    {
        Tail emulatorStdOut = new Tail(logPath.toString());
		this.logListener.add(emulatorStdOut);
		return emulatorStdOut;
    }
}
