package de.bwl.bwfla.emil.datatypes.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class HandleListResponse extends JaxbType{
	@XmlElement(required = true)
	private Collection<String> handles;

	public HandleListResponse(Collection<String> handles) {
		this.handles = handles;
	}
}
