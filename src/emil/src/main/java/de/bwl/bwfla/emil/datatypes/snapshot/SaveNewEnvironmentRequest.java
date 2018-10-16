package de.bwl.bwfla.emil.datatypes.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "newEnvironment")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveNewEnvironmentRequest extends SaveDerivateRequest
{
	@XmlElement(required = true)
	private String title;

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
