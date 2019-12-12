package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImportObjectRequest {
    @XmlElement(required = true)
    private String label;

    @Deprecated
    @XmlElement(defaultValue = "default")
    private String objectArchive = "default";

    @XmlElement
    private List<ImportFileInfo> files;

    public String getLabel() {
        return label;
    }

    public String getObjectArchive() {
        return objectArchive;
    }

    public List<ImportFileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<ImportFileInfo> files) {
        this.files = files;
    }


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ImportFileInfo {
        @XmlElement(required = true)
        private String url;

        @XmlElement(required = true)
        private String deviceId;

        @XmlElement
        private String fileFmt;

        @XmlElement
        private String filename;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getFileFmt() {
            return fileFmt;
        }

        public void setFileFmt(String fileFmt) {
            this.fileFmt = fileFmt;
        }
    }
}
