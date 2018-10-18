package de.bwl.bwfla.emil.datatypes.rest;

import com.mongodb.ReflectionDBObject;
import de.bwl.bwfla.common.datatypes.identification.DiskType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.EnvironmentInfo;
import de.bwl.bwfla.imageclassifier.client.Identification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ClassificationResult extends EmilResponseType {

    @XmlElement(required = true)
    private List<EnvironmentInfo> environmentList;

    @XmlElement
    private List<EnvironmentInfo> manualEnvironmentList;

    @XmlElement(required = true)
    private List<OperatingSystem> suggested;

    @XmlElement
    private HashMap<String, IdentificationData> fileFormatMap;

    @XmlElement(required = true)
    private HashMap<String, DiskType> mediaFormats;

    @XmlElement(required = true)
    private String objectId;

    public ClassificationResult(String objectId,
                                HashMap<String, IdentificationData> fileFormats,
                                HashMap<String, DiskType> mediaFormats) {
        this.objectId = objectId;
        this.fileFormatMap = fileFormats;
        this.mediaFormats = mediaFormats;
    }

    public ClassificationResult(BWFLAException e) {
        super(e);
    }

    public ClassificationResult() {
        environmentList = new ArrayList<>();
        suggested = new ArrayList<>();
        fileFormatMap = new HashMap<>();
        mediaFormats = new HashMap<>();
        manualEnvironmentList = new ArrayList<>();
    }

    public List<EnvironmentInfo> getEnvironmentList() {
        return environmentList;
    }

    public HashMap<String, IdentificationData> getFileFormatMap() {
        return fileFormatMap;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setEnvironmentList(List<EnvironmentInfo> environmentList) {
        this.environmentList = environmentList;
    }

    public List<OperatingSystem> getSuggested() {
        return suggested;
    }

    public void setSuggested(List<OperatingSystem> suggested) {
        this.suggested = suggested;
    }

    public void setFileFormatMap(HashMap<String, IdentificationData> fileFormatMap) {
        this.fileFormatMap = fileFormatMap;
    }

    public boolean hasReferenceTo(String envId) {
        if (environmentList == null || environmentList.size() == 0)
            return false;

        for (EnvironmentInfo info : environmentList) {
            if (info.getId().equals(envId))
                return true;
        }
        return false;
    }

    public HashMap<String, DiskType> getMediaFormats() {
        return mediaFormats;
    }

    public void setMediaFormats(HashMap<String, DiskType> mediaFormats) {
        this.mediaFormats = mediaFormats;
    }


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class IdentificationData extends JaxbType
    {
        @XmlElement(required = true, name = "fileFormats")
        private List<FileFormat> fileFormats;

        public List<FileFormat> getFileFormats() {
            return fileFormats;
        }

        public void setFileFormats(List<FileFormat> fileFormats) {
            this.fileFormats = fileFormats;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class FileFormat extends JaxbType
    {
        @XmlElement(required = true)
        private String puid;

        @XmlElement(required = true)
        private String name;

        @XmlElement(required = true)
        private int count;

        @XmlElement
        private long fromDate;

        @XmlElement
        private long toDate;

        public FileFormat(String puid, String name, int count, long fromDate, long toDate)
        {
            this.name = name;
            this.puid = puid;
            this.count = count;
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        public FileFormat() {}

        public String getPuid() {
            return puid;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public long getFromDate() {
            return fromDate;
        }

        public long getToDate() {
            return toDate;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class OperatingSystem {

        @XmlElement(name="id")
        String id;
        @XmlElement(name="label")
        String label;
        @XmlElement(name="defaultEnvironment")
        EnvironmentInfo defaultEnvironment;

        public OperatingSystem() {}

        public OperatingSystem(String id, String label)
        {
            this.id = id;
            this.label = label;
            this.defaultEnvironment = null;
        }

        public EnvironmentInfo getDefaultEnvironment() {
            return defaultEnvironment;
        }

        public void setDefaultEnvironment(EnvironmentInfo defaultEnvironment) {
            this.defaultEnvironment = defaultEnvironment;
        }
    }
}
