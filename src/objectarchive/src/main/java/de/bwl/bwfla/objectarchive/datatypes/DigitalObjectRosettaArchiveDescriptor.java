package de.bwl.bwfla.objectarchive.datatypes;


import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchiveDescriptor;

public class DigitalObjectRosettaArchiveDescriptor extends DigitalObjectArchiveDescriptor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url; 
	
	
	public DigitalObjectRosettaArchiveDescriptor()
	{
		setType(ArchiveType.ROSETTA);
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
