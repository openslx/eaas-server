package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

public class EnvironmentCreateRequest extends JaxbType{
	private String templateId;
	private String label;
	private String nativeConfig;
	private String size;
	
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getNativeConfig() {
		return nativeConfig;
	}
	public void setNativeConfig(String nativeConfig) {
		this.nativeConfig = nativeConfig;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
}
