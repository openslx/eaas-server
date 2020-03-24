package de.bwl.bwfla.emil.datatypes.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bwl.bwfla.emil.datatypes.snapshot.SnapshotRequest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "saveRevision")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveDerivateRequest extends SnapshotRequest {

	@XmlElement
	private String softwareId = null;

	@XmlElement(defaultValue = "false")
	private boolean cleanRemovableDrives = false;

	public String getSoftwareId() {
		return softwareId;
	}

	public void setSoftwareId(String softwareId) {
		this.softwareId = softwareId;
	}

	public boolean isCleanRemovableDrives() {
		return cleanRemovableDrives;
	}

	public void setCleanRemovableDrives(boolean cleanRemovableDrives) {
		this.cleanRemovableDrives = cleanRemovableDrives;
	}
}
