package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ContainerNetworkingType extends NetworkingType {
    @XmlElement
    private boolean isDHCPenabled;

    @XmlElement(defaultValue = "false")
    private boolean isTelnetEnabled;

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