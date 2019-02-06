package de.bwl.bwfla.emil.datatypes;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmilObjectEnvironment extends EmilEnvironment {
	@XmlElement(required = false)
	protected int driveId;
	@XmlElement(required = false)
	protected String objectId;
	@XmlElement(required = false)
	protected String objectArchiveId;

	public EmilObjectEnvironment(EmilObjectEnvironment template)
	{
		super(template);
		driveId = template.driveId;
		objectId = template.objectId;
		objectArchiveId = template.objectArchiveId;
	}

	public EmilObjectEnvironment(EmilEnvironment template)
	{
		super(template);
	}

	public EmilObjectEnvironment()
	{

	}

	public int getDriveId() {
		return driveId;
	}

	public void setDriveId(int driveId) {
		this.driveId = driveId;
	}

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
}
