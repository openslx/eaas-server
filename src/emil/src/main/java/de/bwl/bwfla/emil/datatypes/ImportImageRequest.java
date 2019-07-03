package de.bwl.bwfla.emil.datatypes;

import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;


@XmlRootElement
public class ImportImageRequest extends JaxbType{
	private String urlString;
	private String templateId;
	private String label;
	private String nativeConfig;
	private String rom;
	private String patchId;

	public String getUrlString() {
		return urlString;
	}
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}
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

	public String getRom() {
		return rom;
	}

	public void setRom(String rom) {
		this.rom = rom;
	}

	public String getPatchId() {
		return patchId;
	}
}
