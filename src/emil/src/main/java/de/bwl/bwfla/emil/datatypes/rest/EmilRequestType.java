package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EmilRequestType extends JaxbType {

    @XmlElement(required = false)
    private String userId;

    public String getUserId() {
        return userId;
    }
}
