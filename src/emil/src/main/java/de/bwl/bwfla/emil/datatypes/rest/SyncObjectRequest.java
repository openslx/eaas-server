package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SyncObjectRequest extends EmilRequestType {

    @XmlElement(required = true)
    private String archive;

    @XmlElement(required = true)
    private List<String> objectIDs;

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public List<String> getObjectIDs() {
        return objectIDs;
    }

    public void setObjectIDs(List<String> objectIDs) {
        this.objectIDs = objectIDs;
    }
}
