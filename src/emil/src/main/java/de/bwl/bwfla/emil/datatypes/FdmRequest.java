package de.bwl.bwfla.emil.datatypes;

import java.util.ArrayList;

import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class FdmRequest {
	private String environment; 
	public static class DataRef {
		private String type;
		private String url;
		private String name = null;
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String toString() { return "type: " + type + "@" + url;}
	}
	
	private ArrayList<DataRef> files;
	
	public String getEnvirnment() {
		return environment;
	}
	public void setEnvironment(String envId) {
		this.environment = envId;
	}
	public ArrayList<DataRef> getFiles() {
		return files;
	}
	public void setFiles(ArrayList<DataRef> files) {
		this.files = files;
	}
	
}
