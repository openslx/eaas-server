package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoricResponse {

    @JsonProperty("newEnvId")
    private String newEnvId;


    public String getMessage() {
        return newEnvId;
    }

    public void setId(String message) {
        this.newEnvId = message;
    }


    public HistoricResponse() {
    }

}
