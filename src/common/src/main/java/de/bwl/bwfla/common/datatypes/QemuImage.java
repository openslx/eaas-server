package de.bwl.bwfla.common.datatypes;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QemuImage extends JaxbType {

    @XmlElement
    @JsonProperty("strVal")
    private String virtualSize;

    @XmlElement
    private String filename;

    @XmlElement
    private String format;

    @XmlElement
    @JsonProperty("backing-filename")
    private String backingFile;

    @XmlElement
    @JsonProperty("full-backing-filename")
    private String fullBackingName;

    public String getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(String virtualSize) {
        this.virtualSize = virtualSize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getBackingFile() {
        return backingFile;
    }

    public void setBackingFile(String backingFile) {
        this.backingFile = backingFile;
    }

    public String getFullBackingName() {
        return fullBackingName;
    }

    public void setFullBackingName(String fullBackingName) {
        this.fullBackingName = fullBackingName;
    }
}
