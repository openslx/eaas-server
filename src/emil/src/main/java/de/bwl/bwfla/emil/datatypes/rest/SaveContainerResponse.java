package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.rest.EmilResponseType;

/**
 * SaveContainerResponse: respond with status and possible id of saved image
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SaveContainerResponse extends EmilResponseType {

    //only if successful
    @XmlElement(required = false)
    private String id;

    public SaveContainerResponse(BWFLAException e)
    {
        super(e);
    }

    public SaveContainerResponse()
    {
        super();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}