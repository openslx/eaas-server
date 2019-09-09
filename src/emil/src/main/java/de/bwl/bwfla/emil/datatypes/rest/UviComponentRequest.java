package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "uvi")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UviComponentRequest extends MachineComponentRequest {

    private String url;
    
    private String filename;
}
