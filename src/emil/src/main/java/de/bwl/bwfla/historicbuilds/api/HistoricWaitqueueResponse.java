package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoricWaitqueueResponse {

    @JsonProperty("status")
    private String status;
    @JsonProperty("id")
    private String id;
    @JsonProperty("resultUrl")
    private String resultUrl;

    public HistoricWaitqueueResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }
}
