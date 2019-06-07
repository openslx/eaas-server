package de.bwl.bwfla.imagearchive.datatypes;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageArchiveMetadata {

    @XmlEnum
    public enum ImageType {
        base, object, user, derivate, system, template, tmp, sessions, roms, containers, checkpoints, patches
    }

    @XmlElement
    private ImageType type;

    @XmlElement
    private String userId;

    @XmlElement
    private String imageId;

    @XmlElement
    private boolean deleteIfExists;

    ImageArchiveMetadata() {
    }

    public ImageArchiveMetadata(ImageType type) {
        this.type = type;
    }

    public ImageType getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isDeleteIfExists() {
        return deleteIfExists;
    }

    public void setDeleteIfExists(boolean deleteIfExists) {
        this.deleteIfExists = deleteIfExists;
    }
}

