package de.bwl.bwfla.prov.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRequest {

    @JsonProperty("environmentId")
    private String environmentId;

    @JsonProperty("inputFiles")
    private Map<String, String> inputFiles;

    @JsonProperty("params")
    private Map<Integer, String> params;


    public WorkflowRequest() {
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public Map<String, String> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(Map<String, String> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public Map<Integer, String> getParams() {
        return params;
    }

    public void setParams(Map<Integer, String> params) {
        this.params = params;
    }
}
