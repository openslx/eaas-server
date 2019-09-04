package de.bwl.bwfla.objectarchive.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import gov.loc.mets.Mets;

import javax.xml.bind.JAXBException;
import java.util.HashMap;

public class DigitalObjectMetadata extends JaxbType{

	private String id;
	private String title;
	private String description;
	private String thumbnail;
	private String summary;
	private String wikiDataId;
	private HashMap<String, String> customData;
	private String metsData;

	DigitalObjectMetadata() {}


	public DigitalObjectMetadata(Mets mets)
	{
		this.id = mets.getID();
		this.title = mets.getLabel1();
		try {
			this.metsData = mets.value(true);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public DigitalObjectMetadata(String id, String title, String description)
	{
		this.id = id;
		this.title = title;
		this.description = description;
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description  = description;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}
	
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public HashMap<String, String> getCustomData() {
		return customData;
	}

	public void setCustomData(HashMap<String, String> customData) {
		this.customData = customData;
	}

	public String getWikiDataId() {
		return wikiDataId;
	}

	public void setWikiDataId(String wikiDataId) {
		this.wikiDataId = wikiDataId;
	}

	public String getMetsData() {
		return metsData;
	}

	public void setMetsData(String metsData) {
		this.metsData = metsData;
	}
}
