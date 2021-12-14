package de.bwl.bwfla.prov.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRequest {

    @JsonProperty("environmentId")
    private String environmentId;

    @JsonProperty("inputTarURL")
    private String inputTarURL;

    @JsonProperty("outputFolder")
    private String outputFolder;

    @JsonProperty("workdirTarURL")
    private String workdirTarURL;

    @JsonProperty("arguments")
    private Map<String, String> arguments;

    @JsonProperty("environmentVariables")
    private Map<String, String> environmentVariables;

    public WorkflowRequest() {
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getInputTarURL() {
        return inputTarURL;
    }

    public void setInputTarURL(String inputTarURL) {
        this.inputTarURL = inputTarURL;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getWorkdirTarURL() {
        return workdirTarURL;
    }

    public void setWorkdirTarURL(String workdirTarURL) {
        this.workdirTarURL = workdirTarURL;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }
}

