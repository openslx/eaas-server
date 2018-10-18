package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PrintJob extends JaxbType {

    @XmlElement
    private String label;

    @XmlElement
    @XmlMimeType("application/octet-stream")
    private DataHandler dataHandler;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }
}
