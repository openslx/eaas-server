package de.bwl.bwfla.historicbuilds.api;

import javax.json.bind.annotation.JsonbProperty;

public class BuildToolchainRequest {
    @JsonbProperty("emulatorID")
    private String emulatorID;
    @JsonbProperty("inputDirectory")
    private String inputDirectory;
    @JsonbProperty("outputDirectory")
    private String outputDirectory;
    @JsonbProperty("execFile")
    private String execFile;
    @JsonbProperty("prerequisites")
    private String[] prerequisites;
    @JsonbProperty("mail")
    private String mail;
    @JsonbProperty("mode")
    private String mode;

    public BuildToolchainRequest() {
    }

    public String getEmulatorID() {
        return emulatorID;
    }

    public void setEmulatorID(String emulatorID) {
        this.emulatorID = emulatorID;
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

    public String getExecFile() {
        return execFile;
    }

    public void setExecFile(String execFile) {
        this.execFile = execFile;
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
