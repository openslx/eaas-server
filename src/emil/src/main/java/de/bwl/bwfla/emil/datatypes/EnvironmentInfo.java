package de.bwl.bwfla.emil.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvironmentInfo extends JaxbType {
    public EnvironmentInfo(String _id, String _label) {
        this.id = _id;
        this.label = _label;
    }
    EnvironmentInfo() {
    }

    @XmlElement(required = true)
    private String id;
    @XmlElement(required = true)
    private String label;
    @XmlElement(defaultValue = "false")
    private boolean objectEnvironment = false;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isObjectEnvironment() {
        return objectEnvironment;
    }

    public void setObjectEnvironment(boolean objectEnvironment) {
        this.objectEnvironment = objectEnvironment;
    }

}