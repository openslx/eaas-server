package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.xml.bind.annotation.*;

@XmlType(name = "uvi")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UviComponentRequest extends MachineComponentRequest {

    @XmlElement
    private String uviUrl;

    @XmlElement
    private String uviFilename;

    @XmlElement (defaultValue = "false")
    private boolean uviWriteable;

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

    public boolean isUviWriteable() {
        return uviWriteable;
    }

}
