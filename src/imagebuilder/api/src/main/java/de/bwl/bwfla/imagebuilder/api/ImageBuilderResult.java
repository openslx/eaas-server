package de.bwl.bwfla.imagebuilder.api;

import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.imagebuilder.api.metadata.ImageBuilderMetadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageBuilderResult {

    @XmlElement(required = true)
    private BlobHandle blobHandle;

    @XmlElement
    private ImageBuilderMetadata metadata;

    public BlobHandle getBlobHandle() {
        return blobHandle;
    }

    public void setBlobHandle(BlobHandle blobHandle) {
        this.blobHandle = blobHandle;
    }

    public ImageBuilderMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ImageBuilderMetadata metadata) {
        this.metadata = metadata;
    }
}
