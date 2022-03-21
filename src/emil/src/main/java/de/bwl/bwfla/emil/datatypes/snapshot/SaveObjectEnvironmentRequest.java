package de.bwl.bwfla.emil.datatypes.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "objectEnvironment")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveObjectEnvironmentRequest extends SnapshotRequest {

	@JsonProperty(required = true)
	@XmlElement(required = true)
	private String objectId;

	@JsonProperty(required = true)
	@XmlElement(required = true)
	private String title;

	@JsonProperty(required = true)
	@XmlElement(required = true)
	private String objectArchiveId;

	@XmlElement
	private int driveId;

	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	
	public String getObjectArchiveId() {
		return objectArchiveId;
	}
	public void setObjectArchiveId(String archiveId) {
		this.objectArchiveId = archiveId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDriveId() {
		return driveId;
	}

	public void setDriveId(int driveId) {
		this.driveId = driveId;
	}
}
