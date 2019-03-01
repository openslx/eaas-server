package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * ImportContainerRequest
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportContainerRequest extends EmilRequestType {

    private String runtimeID;
    private String name;
    private String urlString;

    @XmlElement(required = false)
    private String tag;

    @XmlAttribute(required = false)
    private String digest;

    private String outputFolder;
    private String inputFolder;
    private ContainerImageType imageType;
    private boolean guiRequired;

    private ArrayList<String> processArgs;
    private ArrayList<String> processEnvs;



    /**
     * @return the runtimeID
     */
    public String getRuntimeID() {
        return runtimeID;
    }

    /**
     * @param runtimeID the runtimeID to set
     */
    public void setRuntimeID(String runtimeID) {
        this.runtimeID = runtimeID;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the size
     */
//    public String getSize() {
//        return size;
//    }

    /**
     * @param size the size to set
     */
//    public void setSize(String size) {
//        this.size = size;
//    }


    /**
     * @return the urlString
     */


    public String getUrlString() {
        return urlString;
    }


    public boolean guiRequired() {
        return guiRequired;
    }

    public void setGuiRequired(boolean guiRequired) {
        this.guiRequired = guiRequired;
    }

    /**
     * @param urlString the urlString to set
     */
    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
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

    public String getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

    public String getTag() {
        return tag;
    }

    public ContainerImageType getImageType() {
        return imageType;
    }

    public void setImageType(ContainerImageType imageType) {
        this.imageType = imageType;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    @XmlEnum
    public enum ContainerImageType {
        @XmlEnumValue("rootfs")
        ROOTFS("rootfs"),
        @XmlEnumValue("simg")
        SIMG("simg"),
        @XmlEnumValue("dockerhub")
        DOCKERHUB("dockerhub"),
        @XmlEnumValue("readymade")
        READYMADE("readymade");

        private final String value;

        ContainerImageType(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        public static ContainerImageType fromValue(String v) {
            for (ContainerImageType c: ContainerImageType.values()) {
                if (c.value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }
}