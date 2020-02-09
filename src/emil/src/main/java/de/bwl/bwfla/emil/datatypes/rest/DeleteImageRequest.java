package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DeleteImageRequest {

    @XmlElement
    private String imageId;
    @XmlElement
    private String imageArchive;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageArchive() {
        return imageArchive;
    }

    public void setImageArchive(String imageArchive) {
        this.imageArchive = imageArchive;
    }
}
