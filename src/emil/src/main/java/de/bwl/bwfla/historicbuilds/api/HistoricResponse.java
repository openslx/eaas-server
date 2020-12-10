package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoricResponse {

    @JsonProperty("id")
    private String id;


    public String getMessage() {
        return id;
    }

    public void setId(String message) {
        this.id = message;
    }


    public HistoricResponse() {
    }

}
