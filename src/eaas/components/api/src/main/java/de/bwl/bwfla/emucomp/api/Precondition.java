package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "precondition", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
        "fileSystem",
        "partitionLabel",
        "requiredFiles",
})
public class Precondition {
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
    private String fileSystem;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
    private String partitionLabel;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
    private RequiredFiles requiredFiles;

    public RequiredFiles getRequiredFiles() {
        return requiredFiles;
    }

    public void setRequiredFiles(RequiredFiles requiredFiles) {
        this.requiredFiles = requiredFiles;
    }

    public String getPartitionLabel() {
        return partitionLabel;
    }

    public void setPartitionLabel(String partitionLabel) {
        this.partitionLabel = partitionLabel;
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(String fileSystem) {
        this.fileSystem = fileSystem;
    }
}
