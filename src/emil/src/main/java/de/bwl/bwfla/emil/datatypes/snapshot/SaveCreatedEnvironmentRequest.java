package de.bwl.bwfla.emil.datatypes.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.*;

@XmlType(name = "saveCreatedEnvironment")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveCreatedEnvironmentRequest extends SaveDerivateRequest {

    @XmlElement
    private String title = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
