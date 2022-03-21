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

    @XmlElement(required = false, defaultValue = "default")
    private String archive = "default";

    @XmlElement(required = true)
    private String message;

    @XmlElement(required = false)
    private String author;

    @XmlElement(required = false)
    private boolean isRelativeMouse;

    @XmlElement(defaultValue = "false")
    private boolean cleanRemovableDrives = false;

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

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public boolean isCleanRemovableDrives() {
        return cleanRemovableDrives;
    }

    public void setCleanRemovableDrives(boolean cleanRemovableDrives) {
        this.cleanRemovableDrives = cleanRemovableDrives;
    }
}
