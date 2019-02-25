package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlElement;

public class UpdateLatestEmulatorRequest extends EmilRequestType {

    @XmlElement(required = true)
    private String version;

    @XmlElement(required = true)
    private String emulatorName;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEmulatorName() {
        return emulatorName;
    }

    public void setEmulatorName(String emulatorName) {
        this.emulatorName = emulatorName;
    }
}
