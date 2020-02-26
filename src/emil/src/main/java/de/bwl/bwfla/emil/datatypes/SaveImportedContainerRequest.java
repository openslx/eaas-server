package de.bwl.bwfla.emil.datatypes;

public class SaveImportedContainerRequest {

    private String id;
    private String title;
    private String description;
    private String author;
    private boolean enableNetwork;
    private String runtimeId;
    private boolean serviceContainer;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isEnableNetwork() {
        return enableNetwork;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public boolean isServiceContainer() {
        return serviceContainer;
    }
}
