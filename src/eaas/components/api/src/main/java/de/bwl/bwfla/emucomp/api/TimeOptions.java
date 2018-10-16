package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "timeOptions", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
    "offset",
    "epoch",
})
public class TimeOptions {

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
	private String offset;
	 
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
	private String epoch;
	
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public String getEpoch() {
		return epoch;
	}
	public void setEpoch(String epoch) {
		this.epoch = epoch;
	}
}
