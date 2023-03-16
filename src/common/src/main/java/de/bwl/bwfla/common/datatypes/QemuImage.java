package de.bwl.bwfla.common.datatypes;

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

    @JsonProperty("virtual-size")
    @XmlElement(name = "virtual-size")
    private String virtualSize;

    @JsonProperty("filename")
    @XmlElement(name = "filename")
    private String filename;

    @JsonProperty("format")
    @XmlElement(name = "format")
    private String format;

    @JsonProperty("backing-filename")
    @XmlElement(name = "backing-filename")
    private String backingFile;

    @JsonProperty("backing-filename-format")
    @XmlElement(name = "backing-filename-format")
    private String backingFileFormat;

    @JsonProperty("full-backing-filename")
    @XmlElement(name = "full-backing-filename")
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

    public String getBackingFileFormat() {
        return backingFileFormat;
    }

    public void setBackingFileFormat(String format) {
        this.backingFileFormat = format;
    }

    public String getFullBackingName() {
        return fullBackingName;
    }

    public void setFullBackingName(String fullBackingName) {
        this.fullBackingName = fullBackingName;
    }
}
