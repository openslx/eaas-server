package de.bwl.bwfla.emil.datatypes;

import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

@XmlRootElement
public class ForkRevisionRequest extends JaxbType {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
