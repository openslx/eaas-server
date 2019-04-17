package de.bwl.bwfla.emil.datatypes;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GroupComponent extends JaxbType {
    @XmlElement(required = true)
    private String componentId;
    
    @XmlElement(required = true)
    private String type;
    
    @XmlElement(required = false)
    private URI link;

    @XmlElement(required = false)
    private NetworkResponse networkData;

    public GroupComponent() {
    }

    public GroupComponent(String componentId, String type, URI link) {
        super();
        this.componentId = componentId;
        this.type = type;
        this.link = link;
    }

    public GroupComponent(String componentId, String type, URI link, NetworkResponse controlUrls) {
        super();
        this.componentId = componentId;
        this.type = type;
        this.link = link;
        this.networkData = controlUrls;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URI getLink() {
        return link;
    }

    public void setLink(URI link) {
        this.link = link;
    }

    public NetworkResponse getControlUrls() {
        return networkData;
    }

    public void setControlUrls(NetworkResponse controlUrls) {
        this.networkData = controlUrls;
    }
}
