package de.bwl.bwfla.historicbuilds.api;


import com.fasterxml.jackson.annotation.JsonProperty;


public class HistoricRequest {

    @JsonProperty("softwareHeritage")
    private SoftwareHeritageRequest swhRequest;
    @JsonProperty("buildToolchain")
    private BuildToolchainRequest buildToolchainRequest;

    public HistoricRequest() {
    }

    public SoftwareHeritageRequest getSwhRequest() {
        return swhRequest;
    }

    public void setSwhRequest(SoftwareHeritageRequest swhRequest) {
        this.swhRequest = swhRequest;
    }

    public BuildToolchainRequest getBuildToolchainRequest() {
        return buildToolchainRequest;
    }

    public void setBuildToolchainRequest(BuildToolchainRequest buildToolchainRequest) {
        this.buildToolchainRequest = buildToolchainRequest;
    }
}

