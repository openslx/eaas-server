package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class HandleValueResponse {

	@XmlElement(required = true)
	private Collection<String> values;

	public HandleValueResponse(Collection<String> values) {
		this.values = values;
	}

	public Collection<String> getHandleValues() {
		return values;
	}

	public void setHandleValues(Collection<String> values) {
		this.values = values;
	}
}
