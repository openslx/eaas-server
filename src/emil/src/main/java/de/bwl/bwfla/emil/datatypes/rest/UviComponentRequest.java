package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.*;

@XmlType(name = "uvi")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UviComponentRequest extends MachineComponentRequest {

    @XmlElement
    private String uviUrl;

    @XmlElement
    private String uviFilename;

    public String getUviUrl() {
        return uviUrl;
    }

    public void setUviUrl(String uviUrl) {
        this.uviUrl = uviUrl;
    }

    public String getUviFilename() {
        return uviFilename;
    }

    public void setUviFilename(String uviFilename) {
        this.uviFilename = uviFilename;
    }
}
