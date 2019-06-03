package de.bwl.bwfla.objectarchive.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class SoftwareMetadata extends JaxbType {

    @XmlElement
    private String licence;

    @XmlElement(defaultValue = "-1")
    private int numSeats;

    @XmlElement
    private String QID;

    @XmlElement(defaultValue = "false")
    private boolean isOperatingSystem;

    @XmlElement
    private List<String> supportedFileFormats;


    public String getLicence() {
        return licence;
    }

    public void setLicence(String licence) {
        this.licence = licence;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats = numSeats;
    }

    public String getQID() {
        return QID;
    }

    public void setQID(String QID) {
        this.QID = QID;
    }

    public boolean isOperatingSystem() {
        return isOperatingSystem;
    }

    public void setOperatingSystem(boolean operatingSystem) {
        isOperatingSystem = operatingSystem;
    }

    public List<String> getSupportedFileFormats() {
        return supportedFileFormats;
    }

    public void setSupportedFileFormats(List<String> supportedFileFormats) {
        this.supportedFileFormats = supportedFileFormats;
    }
}
