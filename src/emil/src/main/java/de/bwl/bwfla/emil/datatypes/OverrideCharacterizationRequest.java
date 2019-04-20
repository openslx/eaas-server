package de.bwl.bwfla.emil.datatypes;

import java.util.List;

public class OverrideCharacterizationRequest {
    protected String objectId;
    protected String description;
    protected String objectArchive;

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectId() {
        return objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
    public List<EnvironmentInfo> getEnvironments() {
        return environments;
    }
    public void setEnvironments(List<EnvironmentInfo> environments) {
        this.environments = environments;
    }
    protected List<EnvironmentInfo> environments;

    public String getObjectArchive() {
        return objectArchive;
    }

    public void setObjectArchive(String objectArchive) {
        this.objectArchive = objectArchive;
    }
}
