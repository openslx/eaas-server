package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LinuxRuntimeContainerReq {

    @XmlElement(required = true)
    private String userContainerEnvironment;

    @XmlElement(required = true, defaultValue = "default")
    private String userContainerArchive = "default";

    @XmlElement(required = true)
    private boolean isDHCPenabled;

    @XmlElement(required = false, defaultValue = "false")
    private boolean isTelnetEnabled;

    public String getUserContainerEnvironment() {
        return userContainerEnvironment;
    }

    public void setUserContainerEnvironment(String userContainerEnvironment) {
        this.userContainerEnvironment = userContainerEnvironment;
    }

    public String getUserContainerArchive() {
        return userContainerArchive;
    }

    public void setUserContainerArchive(String userContainerArchive) {
        this.userContainerArchive = userContainerArchive;
    }

    public boolean isDHCPenabled() {
        return isDHCPenabled;
    }

    public void setDHCPenabled(boolean DHCPenabled) {
        isDHCPenabled = DHCPenabled;
    }

    public boolean isTelnetEnabled() {
        return isTelnetEnabled;
    }

    public void setTelnetEnabled(boolean telnetEnabled) {
        isTelnetEnabled = telnetEnabled;
    }
}
