package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.json.bind.annotation.JsonbProperty;
import javax.xml.bind.annotation.XmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true) //TODO remove after testing
public class HistoricRequest {

    @JsonbProperty("softwareHeritage")
    private SoftwareHeritageRequest swhRequest;
    @JsonbProperty("buildToolchain")
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

