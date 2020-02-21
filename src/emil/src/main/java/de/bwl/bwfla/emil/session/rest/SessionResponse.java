package de.bwl.bwfla.emil.session.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.NetworkRequest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SessionResponse extends JaxbType {

    @XmlElement
    private Collection<SessionComponent> components;

    @XmlElement
    private NetworkRequest network;

    public SessionResponse(NetworkRequest request) {
        this.network = request;
        this.components = new ArrayList<>();
    }

    public void add(SessionComponent sc)
    {
        components.add(sc);
    }

    public Collection<SessionComponent> getComponents() {
        return components;
    }

    public NetworkRequest getNetwork() {
        return network;
    }
}
