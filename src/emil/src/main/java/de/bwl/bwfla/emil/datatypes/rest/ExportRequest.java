package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlElement;

public class ExportRequest extends JaxbType {
    @Deprecated
    @XmlElement
    private String envId;

    @XmlElement(defaultValue = "default")
    private String archive;

    @XmlElement(defaultValue = "false")
    private boolean standalone;

    @XmlElement(defaultValue = "false")
    private boolean deleteAfterExport;


    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public boolean isStandalone() {
        return standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public boolean isDeleteAfterExport() {
        return deleteAfterExport;
    }

    public void setDeleteAfterExport(boolean deleteAfterExport) {
        this.deleteAfterExport = deleteAfterExport;
    }
}
