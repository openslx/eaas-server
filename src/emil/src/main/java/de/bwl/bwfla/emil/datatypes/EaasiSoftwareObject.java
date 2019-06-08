package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EaasiSoftwareObject extends JaxbType {

    @XmlElement
    private String metsData;

    @XmlElement
    private SoftwarePackage softwarePackage;

    EaasiSoftwareObject() {}

    public EaasiSoftwareObject(SoftwarePackage p, String mets)
    {
        this.softwarePackage = p;
        this.metsData = mets;
    }

    public String getMetsData() {
        return metsData;
    }

    public void setMetsData(String metsData) {
        this.metsData = metsData;
    }

    public SoftwarePackage getSoftwarePackage() {
        return softwarePackage;
    }

    public void setSoftwarePackage(SoftwarePackage softwarePackage) {
        this.softwarePackage = softwarePackage;
    }
}
