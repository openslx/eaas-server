package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.*;

@XmlRootElement
public class EmilResponseType extends JaxbType {

    @XmlElement(required = true)
    private String status;

    @XmlElement
    private String message;

    public EmilResponseType() {
        status = "0";
    }

    public EmilResponseType(BWFLAException e)
    {
        status = "1";
        message = e.getMessage();
    }

}
