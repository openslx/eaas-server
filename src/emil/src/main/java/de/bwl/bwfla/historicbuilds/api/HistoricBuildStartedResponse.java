package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoricBuildStartedResponse {

    @JsonProperty("taskId")
    private String taskId;

    @JsonProperty("waitQueueUrl")
    private String waitQueueUrl;

    public HistoricBuildStartedResponse() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getWaitQueueUrl() {
        return waitQueueUrl;
    }

    public void setWaitQueueUrl(String waitQueueUrl) {
        this.waitQueueUrl = waitQueueUrl;
    }
}
