package de.bwl.bwfla.emil.datatypes.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bwl.bwfla.emil.datatypes.snapshot.SnapshotRequest;

import javax.xml.bind.annotation.*;

@XmlType(name = "saveUserSession")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveUserSessionRequest extends SnapshotRequest {

    @XmlElement(required = true)
    private String envId;

    @XmlElement(required = true)
    private String objectId;

    @XmlElement(required = false)
    private String archiveId;

    public SaveUserSessionRequest(String envId, String objectId, String archive)
    {
        this.envId = envId;
        this.objectId = objectId;
        this.archiveId = archive;
    }

    public SaveUserSessionRequest(String envId, String objectId)
    {
        this(envId, objectId, null);
    }

    SaveUserSessionRequest() {}

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }
}
