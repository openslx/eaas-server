package de.bwl.bwfla.imagearchive.datatypes;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmulatorMetadata extends JaxbType {

    @XmlElement
    private String emulatorVersion;

    @XmlElement
    private String version;

    @XmlElement
    private String emulatorType;

    @XmlElement
    private String containerDigest;

    @XmlElement
    private String ociSourceUrl;

    public String getEmulatorVersion() {
        return emulatorVersion;
    }

    public void setEmulatorVersion(String emulatorVersion) {
        this.emulatorVersion = emulatorVersion;
    }

    public String getEmulatorType() {
        return emulatorType;
    }

    public void setEmulatorType(String emulatorType) {
        this.emulatorType = emulatorType;
    }

    public String getContainerDigest() {
        return containerDigest;
    }

    public void setContainerDigest(String containerDigest) {
        this.containerDigest = containerDigest;
    }

    public String getOciSourceUrl() {
        return ociSourceUrl;
    }

    public void setOciSourceUrl(String ociSourceUrl) {
        this.ociSourceUrl = ociSourceUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
