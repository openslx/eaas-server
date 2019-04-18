package de.bwl.bwfla.eaas;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ComponentGroupElement extends JaxbType {



    @XmlElement(required = true)
    private String componentId;

    //TODO create a class from networkInfo and update it on detach (if necessary, otherwise delete this field)
    @XmlElement(required = false)
    private String networkInfo;
    @XmlElement(required = false)
    private String customName = null;

    public ComponentGroupElement(String componentId) {
        this.componentId = componentId;
    }

    public ComponentGroupElement() {
    }

    public ComponentGroupElement(String componentId, String customName) {
        this.componentId = componentId;
        this.customName = customName;
    }



    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(String networkInfo) {
        this.networkInfo = networkInfo;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }
}
