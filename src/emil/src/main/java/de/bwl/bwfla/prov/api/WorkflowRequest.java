package de.bwl.bwfla.prov.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRequest {

    @JsonProperty("environmentId")
    private String environmentId;

    @JsonProperty("inputFolder")
    private String inputFolder;

    @JsonProperty("outputFolder")
    private String outputFolder;

    @JsonProperty("inputFiles")
    private Map<String, String> inputFiles;

    @JsonProperty("arguments")
    private Map<String, String> arguments;

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

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public String getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }
}
