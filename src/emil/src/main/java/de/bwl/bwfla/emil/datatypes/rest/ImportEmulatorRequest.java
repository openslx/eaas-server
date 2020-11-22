package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportEmulatorRequest extends ImportContainerRequest {

    @XmlElement(required = true)
    private String version;

    @XmlElement(required = false)
    private String emulatorVersion;

    @XmlElement(required = true)
    private String fstype;

    @XmlElement(required = false)
    private String alias;

    @XmlElement(required = true)
    private String emulatorType;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFstype() {
        return fstype;
    }

    public void setFstype(String fstype) {
        this.fstype = fstype;
    }

    public String getEmulatorType() {
        return emulatorType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setEmulatorType(String emulatorType) {
        this.emulatorType = emulatorType;
    }

    public String getEmulatorVersion() {
        return emulatorVersion;
    }

    public void setEmulatorVersion(String emulatorVersion) {
        this.emulatorVersion = emulatorVersion;
    }
}
