package de.bwl.bwfla.historicbuilds.api;

import javax.json.bind.annotation.JsonbProperty;

public class HistoricRequest {

    @JsonbProperty("softwareHeritage")
    private SoftwareHeritageRequest swhRequest;
    @JsonbProperty("buildToolchain")
    private BuildToolchainRequest buildToolchainRequest;

    public HistoricRequest() {
    }

    public HistoricRequest(SoftwareHeritageRequest swhRequest, BuildToolchainRequest buildToolchainRequest) {
        this.swhRequest = swhRequest;
        this.buildToolchainRequest = buildToolchainRequest;
    }

    public SoftwareHeritageRequest getSwhRequest() {
        return swhRequest;
    }

    public void setSwhRequest(SoftwareHeritageRequest swhRequest) {
        this.swhRequest = swhRequest;
    }

    public BuildToolchainRequest getBuildToolchainRequest() {
        return buildToolchainRequest;
    }

    public void setBuildToolchainRequest(BuildToolchainRequest buildToolchainRequest) {
        this.buildToolchainRequest = buildToolchainRequest;
    }

    private class SoftwareHeritageRequest {
        @JsonbProperty(value = "revisionId", nillable = true)
        private String revisionId;
        @JsonbProperty(value = "directoryId", nillable = true)
        private String directoryId;
        @JsonbProperty("extract")
        private boolean extract;

        public SoftwareHeritageRequest() {
        }

        public SoftwareHeritageRequest(String revisionId, String directoryId, boolean extract) {
            this.revisionId = revisionId;
            this.directoryId = directoryId;
            this.extract = extract;
        }

        public String getRevisionId() {
            return revisionId;
        }

        public void setRevisionId(String revisionId) {
            this.revisionId = revisionId;
        }

        public String getDirectoryId() {
            return directoryId;
        }

        public void setDirectoryId(String directoryId) {
            this.directoryId = directoryId;
        }

        public boolean isExtract() {
            return extract;
        }

        public void setExtract(boolean extract) {
            this.extract = extract;
        }
    }

    private class BuildToolchainRequest {
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

        public BuildToolchainRequest(String emulatorID, String inputDirectory,
                                     String outputDirectory, String execFile,
                                     String[] prerequisites, String mail, String mode) {
            this.emulatorID = emulatorID;
            this.inputDirectory = inputDirectory;
            this.outputDirectory = outputDirectory;
            this.execFile = execFile;
            this.prerequisites = prerequisites;
            this.mail = mail;
            this.mode = mode;
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

}

