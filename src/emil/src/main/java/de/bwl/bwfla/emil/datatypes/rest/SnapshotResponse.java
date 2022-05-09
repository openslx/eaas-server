package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.rest.EmilResponseType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SnapshotResponse extends EmilResponseType
{
    @XmlElement(required = true)
    private String envId;

    public SnapshotResponse(BWFLAException e)
    {
        super(e);
    }

    public SnapshotResponse(String envId)
    {
        this.envId = envId;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }
}
