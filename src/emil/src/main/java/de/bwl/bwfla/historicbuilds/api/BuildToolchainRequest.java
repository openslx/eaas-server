package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonProperty;


public class BuildToolchainRequest {
    @JsonProperty("environmentID")
    private String environmentID;
    @JsonProperty("inputDirectory")
    private String inputDirectory;
    @JsonProperty("outputDirectory")
    private String outputDirectory;
    @JsonProperty("recipe")
    private String recipe;
    @JsonProperty("prerequisites")
    private String[] prerequisites;
    @JsonProperty("mail")
    private String mail;
    @JsonProperty("mode")
    private String mode;

    public BuildToolchainRequest() {
    }

    public String getEnvironmentID() {
        return environmentID;
    }

    public void setEnvironmentID(String environmentID) {
        this.environmentID = environmentID;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String[] getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String[] prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
