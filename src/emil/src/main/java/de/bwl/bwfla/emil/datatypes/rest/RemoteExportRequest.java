package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RemoteExportRequest extends EmilRequestType {

    @XmlElement(required = true)
    private String wsHost;

    @XmlElement(required = true)
    private List<String> envId;

    @XmlElement(required = false, defaultValue = "false")
    private boolean exportObjectEmbedded;

    @XmlElement(required = false)
    private String objectArchiveHost;

    public String getWsHost() {
        return wsHost;
    }

    public void setWsHost(String wsHost) {
        this.wsHost = wsHost;
    }

    public List<String> getEnvId() {
        return envId;
    }

    public void setEnvId(List<String> envId) {
        this.envId = envId;
    }

    public boolean isExportObjectEmbedded() {
        return exportObjectEmbedded;
    }

    public String getObjectArchiveHost() {
        return objectArchiveHost;
    }

    public void setObjectArchiveHost(String objectArchiveHost) {
        this.objectArchiveHost = objectArchiveHost;
    }
}
