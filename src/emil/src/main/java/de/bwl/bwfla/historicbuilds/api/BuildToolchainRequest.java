package de.bwl.bwfla.historicbuilds.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonProperty(value = "cronUser")
    private String cronUser;
    @JsonProperty("autoStart")
    private Boolean autoStart;
    @JsonProperty("recipeName")
    private String recipeName;
    @JsonProperty("recipeLocation")
    private String recipeLocation;
    @JsonProperty("logFileLocation")
    private String logFileLocation;
    @JsonProperty("injectOnly")
    private Boolean injectOnly;
    @JsonProperty("additionalInjects")
    private AdditionalInjectRequest[] additionalInjects;



    public BuildToolchainRequest() {
        //defaults
        recipeName = "recipe.sh";
        autoStart = true;
        cronUser = "root";
        recipeLocation = "/";
        logFileLocation = "/swh-log.txt";
        injectOnly = false;
        additionalInjects = null;
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

    public String getCronUser() {
        return cronUser;
    }

    public void setCronUser(String cronUser) {
        this.cronUser = cronUser;
    }

    public Boolean getAutoStart() {
        return autoStart;
    }

    public void setAutoStart(Boolean autoStart) {
        this.autoStart = autoStart;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeLocation() {
        return recipeLocation;
    }

    public void setRecipeLocation(String recipeLocation) {
        this.recipeLocation = recipeLocation;
    }

    public String getLogFileLocation() {
        return logFileLocation;
    }

    public void setLogFileLocation(String logFileLocation) {
        this.logFileLocation = logFileLocation;
    }

    public Boolean getInjectOnly() {
        return injectOnly;
    }

    public void setInjectOnly(Boolean injectOnly) {
        this.injectOnly = injectOnly;
    }

    public AdditionalInjectRequest[] getAdditionalInjects() {
        return additionalInjects;
    }

    public void AdditionalInjectRequest(AdditionalInjectRequest[] additionalInjects) {
        this.additionalInjects = additionalInjects;
    }
}
