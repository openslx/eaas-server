package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EnvironmentMetaData extends EmilResponseType {

    @XmlElement(required = true)
    private boolean mediaChangeSupport;

    public boolean isMediaChangeSupport() {
        return mediaChangeSupport;
    }

    public EnvironmentMetaData() {}

    public EnvironmentMetaData(BWFLAException e) {
        super(e);
    }

    public void setMediaChangeSupport(boolean mediaChangeSupport) {
        this.mediaChangeSupport = mediaChangeSupport;
    }
}
