package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EmilRequestType extends JaxbType {

    @XmlElement(required = false)
    private String userId;

    @XmlElement(required = false)
    private boolean connectEnvs;

    @XmlElement(required = false)
    private NetworkingType networking;

    public boolean canConnectEnvs() {
        return connectEnvs;
    }

    public void setConnectEnvs(boolean connectEnvs) {
        this.connectEnvs = connectEnvs;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isConnectEnvs() {
        return connectEnvs;
    }

    public NetworkingType getNetworking() {
        return networking;
    }

    public void setNetworking(NetworkingType networking) {
        this.networking = networking;
    }
}
