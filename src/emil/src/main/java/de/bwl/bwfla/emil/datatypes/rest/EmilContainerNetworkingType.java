package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmilContainerNetworkingType extends EmilNetworkingType {
    @XmlElement
    private boolean isDHCPenabled;

    public boolean isDHCPenabled() {
        return isDHCPenabled;
    }

    public void setDHCPenabled(boolean DHCPenabled) {
        isDHCPenabled = DHCPenabled;
    }
}