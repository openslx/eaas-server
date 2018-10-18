package de.bwl.bwfla.emil.datatypes;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RuntimeListResponse extends JaxbType {

    @XmlElement(required = true)
    private String status;

    @XmlElement(required = true)
    private ArrayList<RuntimeListItem> runtimes = new ArrayList<>();

    public RuntimeListResponse() {
        this.status = "0";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<RuntimeListItem> getRuntimes() {
        return runtimes;
    }

    public void setRuntimes(ArrayList<RuntimeListItem> runtimes) {
        this.runtimes = runtimes;
    }


}