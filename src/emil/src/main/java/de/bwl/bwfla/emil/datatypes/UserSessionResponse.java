package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.emil.datatypes.rest.EmilResponseType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserSessionResponse extends EmilResponseType {

    @XmlElement(required = true)
    String envId;

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

}
