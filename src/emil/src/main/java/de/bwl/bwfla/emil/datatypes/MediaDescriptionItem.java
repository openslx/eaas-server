package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MediaDescriptionItem extends JaxbType {

    @XmlElement (required = true)
    String id;

    @XmlElement (required = false)
    String label;

    public MediaDescriptionItem(String id)
    {
        this.id = id;
        this.label = id;
    }

    public MediaDescriptionItem(String id, String label)
    {
        this.id = id;
        this.label = label;
    }

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
}
