package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;


public class SoftwareHeritageRequest {
    @JsonProperty("revisionId")
    private String revisionId;
    @JsonProperty("directoryId")
    private String directoryId;
    @JsonProperty("extract")
    private boolean extract;
    @JsonProperty("scriptLocation")
    private String scriptLocation;


    public SoftwareHeritageRequest() {
    }

    public String getScriptLocation() {
        return scriptLocation;
    }

    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public boolean isExtract() {
        return extract;
    }

    public void setExtract(boolean extract) {
        this.extract = extract;
    }
}
