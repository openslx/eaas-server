package de.bwl.bwfla.objectarchive.datatypes;

import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchiveDescriptor;

public class DigitalObjectPreservicaArchiveDescriptor extends DigitalObjectArchiveDescriptor {

	private String collectionId;
	private String sdbHost;
	private String username = null;
	private String password = null;
	

	public String getCollectionId() {
		return collectionId;
	}
	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}
	public String getSdbHost() {
		return sdbHost;
	}
	public void setSdbHost(String sdbHost) {
		this.sdbHost = sdbHost;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
