package de.bwl.bwfla.emil.datatypes.rest;


import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UploadResponse extends EmilResponseType {

    @XmlElement(required = true)
    private List<String> uploads;

    public UploadResponse(BWFLAException e) {
        super(e);
    }

    public UploadResponse() {}

    public List<String> getUploads() {
        if(uploads == null)
            uploads = new ArrayList<>();

        return uploads;
    }
}
