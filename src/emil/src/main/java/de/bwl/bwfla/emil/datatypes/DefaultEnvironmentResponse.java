package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.rest.EmilResponseType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DefaultEnvironmentResponse extends EmilResponseType {

    @XmlElement(required = true)
    private String envId;

    public DefaultEnvironmentResponse(BWFLAException e)
    {
        super(e);
    }

    public DefaultEnvironmentResponse()
    {

    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

}
