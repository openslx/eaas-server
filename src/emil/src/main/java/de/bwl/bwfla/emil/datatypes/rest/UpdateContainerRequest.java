package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = false)
public class UpdateContainerRequest extends EmilRequestType {


    private String id;
    private String title;
    private String description;
    private String outputFolder;
    private String inputFolder;
    private String author;
    private String containerRuntimeId;

    private ArrayList<String> processArgs;
    private ArrayList<String> processEnvs;
    @XmlElement(required = false)
    private EmilContainerNetworkingType networking;

    @Override
    public EmilContainerNetworkingType getNetworking() {
        return networking;
    }

    public void setNetworking(EmilContainerNetworkingType networking) {
        this.networking = networking;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

    public ArrayList<String> getProcessArgs() {
        return processArgs;
    }

    public void setProcessArgs(ArrayList<String> processArgs) {
        this.processArgs = processArgs;
    }

    public ArrayList<String> getProcessEnvs() {
        return processEnvs;
    }

    public void setProcessEnvs(ArrayList<String> processEnvs) {
        this.processEnvs = processEnvs;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContainerRuntimeId() {
        return containerRuntimeId;
    }

    public void setContainerRuntimeId(String containerRuntimeId) {
        this.containerRuntimeId = containerRuntimeId;
    }
}
