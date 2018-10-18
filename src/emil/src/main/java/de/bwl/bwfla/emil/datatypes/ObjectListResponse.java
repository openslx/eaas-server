package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ObjectListResponse extends JaxbType{

    @XmlElement(required = true)
    private String status;

    @XmlElement(required = true)
    ArrayList<ObjectListItem> objects = new ArrayList<>();

    public ObjectListResponse()
    {
        this.status = "0";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<ObjectListItem> getObjects() {
        return objects;
    }

    public void setObjects(ArrayList<ObjectListItem> objects) {
        this.objects = objects;
    }
}
