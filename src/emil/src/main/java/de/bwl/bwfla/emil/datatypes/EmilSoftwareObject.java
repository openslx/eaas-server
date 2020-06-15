package de.bwl.bwfla.emil.datatypes;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmilSoftwareObject {
	
	/*
	 * {"objectId":"id","licenseInformation":"","allowedInstances":1,"nativeFMTs":[],"importFMTs":[],"exportFMTs":[]}
	 */

	private String objectId;
	private String label;
	private String licenseInformation;
	private int allowedInstances;
	private String QID;
	private ArrayList<String> nativeFMTs;
	private ArrayList<String> importFMTs;
	private ArrayList<String> exportFMTs;
	private String archiveId;

	@JsonProperty
	private boolean isPublic;

	@JsonProperty
	private boolean isOperatingSystem;
	
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getLicenseInformation() {
		return licenseInformation;
	}
	public void setLicenseInformation(String licenseInformation) {
		this.licenseInformation = licenseInformation;
	}
	public int getAllowedInstances() {
		return allowedInstances;
	}
	public void setAllowedInstances(int allowedInstances) {
		this.allowedInstances = allowedInstances;
	}
	public ArrayList<String> getNativeFMTs() {
		return nativeFMTs;
	}
	public void setNativeFMTs(List<String> list) {
		if(list != null)
			this.nativeFMTs = new ArrayList<String>(list);
	}
	public ArrayList<String> getImportFMTs() {
		return importFMTs;
	}
	public void setImportFMTs(List<String> importFMTs) {
		if(importFMTs != null)
			this.importFMTs = new ArrayList<String>(importFMTs);
	}
	public ArrayList<String> getExportFMTs() {
		return exportFMTs;
	}
	public void setExportFMTs(List<String> exportFMTs) {
		if(exportFMTs != null)
			this.exportFMTs = new ArrayList<String>(exportFMTs);
	}
	public String getArchiveId() {
		return archiveId;
	}
	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setIsOperatingSystem(boolean os)
	{
		this.isOperatingSystem = os;
	}
	
	public boolean getIsOperatingSystem()
	{
		return this.isOperatingSystem;
	}

	public String getQID() {
		return QID;
	}

	public void setQID(String QID) {
		this.QID = QID;
	}

	public boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(boolean aPublic) {
		isPublic = aPublic;
	}
}
