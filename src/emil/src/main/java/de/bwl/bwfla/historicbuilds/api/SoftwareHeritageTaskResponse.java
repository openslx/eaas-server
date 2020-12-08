package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SoftwareHeritageTaskResponse {

    @JsonProperty("path")
    private String path;

    @JsonProperty("extracted")
    boolean isExtracted;

    public SoftwareHeritageTaskResponse() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isExtracted() {
        return isExtracted;
    }

    public void setExtracted(boolean extracted) {
        isExtracted = extracted;
    }
}
