package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoricResponse {

    @JsonProperty("message")
    private String message;
    @JsonProperty("swhTaskId")
    private String swhTaskId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSwhTaskId() {
        return swhTaskId;
    }

    public void setSwhTaskId(String swhTaskId) {
        this.swhTaskId = swhTaskId;
    }

    public String getBuildToolchainTaskId() {
        return buildToolchainTaskId;
    }

    public void setBuildToolchainTaskId(String buildToolchainTaskId) {
        this.buildToolchainTaskId = buildToolchainTaskId;
    }

    @JsonProperty("buildToolchainTaskId")
    private String buildToolchainTaskId;

    public HistoricResponse() {
    }

}
