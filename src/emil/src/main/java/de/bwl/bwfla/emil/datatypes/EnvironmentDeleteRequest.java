package de.bwl.bwfla.emil.datatypes;

import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvironmentDeleteRequest {
	@Deprecated
    private String envId;  // TODO: remove it!
    private boolean deleteMetaData = false;
    private boolean deleteImage = false;
    private boolean force = true;

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

	public boolean getDeleteMetaData() {
		return deleteMetaData;
	}

	public void setDeleteMetaData(boolean deleteMetaData) {
		this.deleteMetaData = deleteMetaData;
	}

	public boolean getDeleteImage() {
		return deleteImage;
	}

	public void setDeleteImage(boolean deleteImage) {
		this.deleteImage = deleteImage;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}
}
