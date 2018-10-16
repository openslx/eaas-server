package de.bwl.bwfla.objectarchive.datatypes.bsb;

import de.bwl.bwfla.emucomp.api.Drive.DriveType;

public class BsbFileCollectionEntry {
	private DriveType type;
	private String fileId;
	private String fileOriginalName;
	private String url;
	public DriveType getType() {
		return type;
	}
	public void setType(DriveType type) {
		this.type = type;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileOriginalName() {
		return fileOriginalName;
	}
	public void setFileOriginalName(String fileOriginalName) {
		this.fileOriginalName = fileOriginalName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
