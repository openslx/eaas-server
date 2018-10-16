package de.bwl.bwfla.imagearchive.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageImportResult
{
    @XmlElement(required = true)
    private String urlPrefix;

    @XmlElement(required = true)
    private String imageId;


    ImageImportResult() {
    }

    public ImageImportResult(String urlPrefix, String imageId) {
        this.urlPrefix = urlPrefix;
        this.imageId = imageId;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public String getImageId() {
        return imageId;
    }

    public void setUrlPrefix(String prefix) {
        this.urlPrefix = prefix;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
