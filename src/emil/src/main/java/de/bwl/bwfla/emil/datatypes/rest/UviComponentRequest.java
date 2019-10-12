package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

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

    @XmlElement
    private List<UviFile> auxFiles;

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

    public List<UviFile> getAuxFiles() {
        if (auxFiles == null)
            auxFiles = new ArrayList<>();

        return auxFiles;
    }

    @XmlRootElement
    public class UviFile
    {
        @XmlElement
        private String url;

        @XmlElement
        private String filename;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }
}
