package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BuildToolchainTaskResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("envType")
    private String envType;

    public BuildToolchainTaskResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnvType() {
        return envType;
    }

    public void setEnvType(String envType) {
        this.envType = envType;
    }


}
