package de.bwl.bwfla.emil.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MediaDescriptionTypeList {

    @XmlElement(required = true)
    private String mediumType;

    @XmlElement(required = true)
    private ArrayList<MediaDescriptionItem> items = new ArrayList<>();

    public String getMediumType() {
        return mediumType;
    }

    public void setMediumType(String mediumType) {
        this.mediumType = mediumType;
    }

    public ArrayList<MediaDescriptionItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<MediaDescriptionItem> items) {
        this.items = items;
    }
}
