package de.bwl.bwfla.emil.datatypes.snapshot;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.bwl.bwfla.emil.datatypes.rest.*;

@XmlSeeAlso({SaveDerivateRequest.class,
        SaveObjectEnvironmentRequest.class,
        SaveNewEnvironmentRequest.class,
        SaveUserSessionRequest.class,
        SaveImportRequest.class,
        SaveCreatedEnvironmentRequest.class
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SnapshotRequest  extends EmilRequestType {

    @XmlElement(required = true)
    private String envId;

    @XmlElement(required = true)
    private String message;

    @XmlElement(required = false)
    private String author;

    @XmlElement(required = false)
    private boolean isRelativeMouse;

    public boolean isRelativeMouse() {
        return isRelativeMouse;
    }

    public void setRelativeMouse(boolean relativeMouse) {
        isRelativeMouse = relativeMouse;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }
}
