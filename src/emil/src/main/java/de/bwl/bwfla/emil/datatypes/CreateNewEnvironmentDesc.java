package de.bwl.bwfla.emil.datatypes;

import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateNewEnvironmentDesc {

	private String label;
	private String templateId;
	private String imageId;
	private String objectId;
	private String nativeConfig;
	
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getNativeConfig() {
		return nativeConfig;
	}
	public void setNativeConfig(String nativeConfig) {
		this.nativeConfig = nativeConfig;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
