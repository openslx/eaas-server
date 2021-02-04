package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportEmulatorRequest extends EmilRequestType {

    @XmlElement
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }
}
