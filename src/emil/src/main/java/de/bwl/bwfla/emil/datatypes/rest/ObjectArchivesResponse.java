package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ObjectArchivesResponse extends EmilResponseType {

    @XmlElement
    private List<String> archives;

    public ObjectArchivesResponse(BWFLAException e) {
        super(e);
    }

    public ObjectArchivesResponse() {}


    public List<String> getArchives() {
        return archives;
    }

    public void setArchives(List<String> archives) {
        this.archives = archives;
    }
}
