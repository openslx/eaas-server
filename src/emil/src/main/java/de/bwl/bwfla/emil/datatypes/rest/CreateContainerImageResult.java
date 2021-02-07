package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateContainerImageResult extends JaxbType {
    @XmlElement
    private String containerUrl;

    @XmlElement
    private ContainerImageMetadata metadata;

    public String getContainerUrl() {
        return containerUrl;
    }

    public void setContainerUrl(String containerUrl) {
        this.containerUrl = containerUrl;
    }

    public ContainerImageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ContainerImageMetadata metadata) {
        this.metadata = metadata;
    }

    @XmlRootElement
    public static class ContainerImageMetadata {
        @XmlElement
        private String containerSourceUrl;

        @XmlElement
        private List<String> entryProcesses;

        @XmlElement
        private List<String> envVariables;

        @XmlElement
        private String workingDir;

        @XmlElement
        private String containerDigest;

        @XmlElement
        private String tag;

        @XmlElement
        private String emulatorType;

        @XmlElement
        private String emulatorVersion;

        public String getContainerSourceUrl() {
            return containerSourceUrl;
        }

        public void setContainerSourceUrl(String containerSourceUrl) {
            this.containerSourceUrl = containerSourceUrl;
        }

        public List<String> getEntryProcesses() {
            return entryProcesses;
        }

        public void setEntryProcesses(List<String> entryProcesses) {
            this.entryProcesses = entryProcesses;
        }

        public List<String> getEnvVariables() {
            return envVariables;
        }

        public void setEnvVariables(List<String> envVariables) {
            this.envVariables = envVariables;
        }

        public String getWorkingDir() {
            return workingDir;
        }

        public void setWorkingDir(String workingDir) {
            this.workingDir = workingDir;
        }

        public String getContainerDigest() {
            return containerDigest;
        }

        public void setContainerDigest(String containerDigest) {
            this.containerDigest = containerDigest;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getEmulatorType() {
            return emulatorType;
        }

        public void setEmulatorType(String emulatorType) {
            this.emulatorType = emulatorType;
        }

        public String getEmulatorVersion() {
            return emulatorVersion;
        }

        public void setEmulatorVersion(String emulatorVersion) {
            this.emulatorVersion = emulatorVersion;
        }
    }
}
