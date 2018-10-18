package de.bwl.bwfla.emucomp.control.connectors;


import java.net.URI;

public class XpraConnector implements IConnector {

    int port;

    public final static String PROTOCOL = "xpra";

    public XpraConnector(int port) {
        this.port = port;
    }


    @Override
    public URI getControlPath(final URI componentResource) {
        return componentResource.resolve(XpraConnector.PROTOCOL);
    }

    @Override
    public String getProtocol() {
        return XpraConnector.PROTOCOL;
    }

    public int getPort() {
        return port;
    }
}
