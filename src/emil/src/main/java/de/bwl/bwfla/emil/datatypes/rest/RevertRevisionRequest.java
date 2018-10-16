package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

@XmlRootElement
public class RevertRevisionRequest extends JaxbType {

	private String currentId;
	private String revId;
	
	public String getCurrentId() {
		return currentId;
	}
	public void setCurrentId(String currentId) {
		this.currentId = currentId;
	}
	public String getRevId() {
		return revId;
	}
	public void setRevId(String revId) {
		this.revId = revId;
	}
}
