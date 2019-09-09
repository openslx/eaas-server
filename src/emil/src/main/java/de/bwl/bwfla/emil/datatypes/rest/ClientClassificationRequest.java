package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ClientClassificationRequest extends JaxbType {

    @XmlElement(required = true)
    private String archiveId;

    @XmlElement(required = true)
    private String objectId;

    @XmlElement(defaultValue = "false")
    private boolean updateClassification;

    @XmlElement(defaultValue = "false")
    private boolean updateProposal;

    @XmlElement(defaultValue = "false")
    private boolean noUpdate;

    @XmlElement
    private String url;

    @XmlElement
    private String filename;

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public boolean isUpdateClassification() {
        return updateClassification;
    }

    public void setUpdateClassification(boolean updateClassification) {
        this.updateClassification = updateClassification;
    }

    public boolean isUpdateProposal() {
        return updateProposal;
    }

    public void setUpdateProposal(boolean updateProposal) {
        this.updateProposal = updateProposal;
    }

    public boolean isNoUpdate() {
        return noUpdate;
    }

    public void setNoUpdate(boolean noUpdate) {
        this.noUpdate = noUpdate;
    }

    public String getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }
}
