package de.bwl.bwfla.emil.datatypes.rest;


import de.bwl.bwfla.common.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.FileCollection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MediaDescriptionResponse extends EmilResponseType {

    @XmlElement(required = true)
    private FileCollection mediaItems;

    @XmlElement(required = true)
    private DigitalObjectMetadata metadata;

    @XmlElement
    private ClassificationResult objectEnvironments;

    public MediaDescriptionResponse ()
    {

    }

    public MediaDescriptionResponse(BWFLAException e)
    {
        super(e);
    }


    public FileCollection getMediaItems() {
        return mediaItems;
    }

    public void setMediaItems(FileCollection mediaItems) {
        this.mediaItems = mediaItems;
    }

    public DigitalObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DigitalObjectMetadata metadata) {
        this.metadata = metadata;
    }

    public ClassificationResult getObjectEnvironments() {
        return objectEnvironments;
    }

    public void setObjectEnvironments(ClassificationResult objectEnvironments) {
        this.objectEnvironments = objectEnvironments;
    }
}
