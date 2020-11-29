package de.bwl.bwfla.historicbuilds.api;

import javax.json.bind.annotation.JsonbProperty;

public class SoftwareHeritageRequest {
    @JsonbProperty(value = "revisionId", nillable = true)
    private String revisionId;
    @JsonbProperty(value = "directoryId", nillable = true)
    private String directoryId;
    @JsonbProperty("extract")
    private boolean extract;

    public SoftwareHeritageRequest() {
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
