package de.bwl.bwfla.prov.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InjectedEnvironmentResponse {


    @JsonProperty("environmentId")
    private String environmentId;

    public InjectedEnvironmentResponse() {
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

}
