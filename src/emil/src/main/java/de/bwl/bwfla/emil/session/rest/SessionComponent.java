package de.bwl.bwfla.emil.session.rest;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.NetworkResponse;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SessionComponent extends JaxbType {
    @XmlElement(required = true)
    private String componentId;

    @XmlElement(required = true)
    private String environmentId;
    
    @XmlElement(required = true)
    private String type;

    @XmlElement(required = false)
    private String networkLabel;

    @XmlElement
    private NetworkResponse networkData;

    SessionComponent() { }

    public SessionComponent(String componentId, String type, String environmentId) {
        super();
        this.componentId = componentId;
        this.type = type;
        this.environmentId = environmentId;
    }

    public SessionComponent(String componentId, String type, String environmentId, String networkLabel) {
        super();
        this.componentId = componentId;
        this.type = type;
        this.environmentId = environmentId;
        this.networkLabel = networkLabel;
    }

    public void addNetworkData(NetworkResponse networkResponse) {
        this.networkData = networkResponse;
    }
}
