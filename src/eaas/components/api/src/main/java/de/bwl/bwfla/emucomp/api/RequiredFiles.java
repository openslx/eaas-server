package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "requiredFiles", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
        "fileName",
})
public class RequiredFiles {

    @XmlElement(name = "fileName", namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
    private String[] fileName;

    public String[] getFileNames() {
        return fileName;
    }

    public void setFileName(String[] fileName) {
        this.fileName = fileName;
    }
}
