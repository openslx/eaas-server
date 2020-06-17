package de.bwl.bwfla.imagebuilder.api.metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DockerImport extends ImageBuilderMetadata {

    @XmlElement
    private String imageRef;

    @XmlElement
    private String tag;

    @XmlElement
    private List<String> layers;

    @XmlElement
    private String emulatorVersion;

    @XmlElement
    private String digest;

    @XmlElement
    private String emulatorType;

    @XmlElement
    private ArrayList<String> entryProcesses;

    @XmlElement
    private ArrayList<String> envVariables;

    @XmlElement
    private String workingDir;

    public String getImageRef() {
        return imageRef;
    }

    public void setImageRef(String imageRef) {
        this.imageRef = imageRef;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getLayers() {
        return layers;
    }

    public void setLayers(List layers) {
        this.layers = layers;
    }

    public String getEmulatorVersion() {
        return emulatorVersion;
    }

    public void setEmulatorVersion(String emulatorVersion) {
        this.emulatorVersion = emulatorVersion;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getEmulatorType() {
        return emulatorType;
    }

    public void setEmulatorType(String emulatorType) {
        this.emulatorType = emulatorType;
    }

    public ArrayList getEntryProcesses() {
        return entryProcesses;
    }

    public void setEntryProcesses(ArrayList entryProcesses) {
        this.entryProcesses = entryProcesses;
    }

    public ArrayList getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(ArrayList envVariables) {
        this.envVariables = envVariables;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }
}
