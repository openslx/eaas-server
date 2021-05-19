package de.bwl.bwfla.prov.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowWaitqueueResponse {

    @Deprecated
    @JsonProperty("status")
    private String status;
    @JsonProperty("id")
    private String id;
    @JsonProperty("resultUrl")
    private String resultUrl;

    @JsonProperty("isDone")
    private Boolean isDone;

    @JsonProperty("hasError")
    private Boolean hasError;

    public WorkflowWaitqueueResponse() {
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

    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }
}
