package de.bwl.bwfla.emil.datatypes.rest;


import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.ws.rs.DefaultValue;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MediaChangeRequest extends JaxbType {
    private String objectId;
    private String driveId;
    private String label;

    @XmlElement(defaultValue = "default")
    private String archiveId;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }
}
