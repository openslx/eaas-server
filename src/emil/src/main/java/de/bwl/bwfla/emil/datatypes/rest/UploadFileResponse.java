package de.bwl.bwfla.emil.datatypes.rest;


import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UploadFileResponse extends EmilResponseType {

    @XmlElement(required = true)
    String userDataUrl;

    public UploadFileResponse(BWFLAException e) {
        super(e);
    }

    public UploadFileResponse() {}

    public String getUserDataUrl() {
        return userDataUrl;
    }

    public void setUserDataUrl(String userDataUrl) {
        this.userDataUrl = userDataUrl;
    }
}
