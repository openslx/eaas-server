package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.*;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateContainerImageRequest {
    @XmlElement(required = false)
    private String tag;

    @XmlAttribute(required = false)
    private String digest;

    @XmlElement
    private ContainerType containerType;

    @XmlElement
    private String urlString;

    @XmlElement(defaultValue = "false")
    private boolean checkForExistingDigest;


    public String getTag() {
        return tag;
    }

    public String getDigest() {
        return digest;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public String getUrlString() {
        return urlString;
    }

    public boolean shouldCheckForExistingDigest() {
        return checkForExistingDigest;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public void setCheckForExistingDigest(boolean checkForExistingDigest) {
        this.checkForExistingDigest = checkForExistingDigest;
    }

    @XmlEnum
    public enum ContainerType {
        @XmlEnumValue("rootfs")
        ROOTFS("rootfs"),
        @XmlEnumValue("simg")
        SIMG("simg"),
        @XmlEnumValue("dockerhub")
        DOCKERHUB("dockerhub"),
        @XmlEnumValue("readymade")
        READYMADE("readymade");

        private final String value;

        ContainerType(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        public static ContainerType fromValue(String v) {
            for (ContainerType c : ContainerType.values()) {
                if (c.value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }
}
