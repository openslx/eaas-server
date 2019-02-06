package de.bwl.bwfla.imagearchive.datatypes;


import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageExport {

    @XmlElement
    private List<ImageFileInfo> imageFiles;

    public List<ImageFileInfo> getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(List<ImageFileInfo> imageFiles) {
        this.imageFiles = imageFiles;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    static public class ImageFileInfo {

        @XmlElement
        String urlPrefix;

        @XmlElement
        String id;

        @XmlElement
        ImageArchiveMetadata.ImageType type;

        ImageFileInfo() {}

        public ImageFileInfo(String prefix, String id, ImageArchiveMetadata.ImageType type)
        {
            this.urlPrefix = prefix;
            this.id = id;
            this.type = type;
        }

        public String getUrlPrefix() {
            return urlPrefix;
        }

        public String getId() {
            return id;
        }

        public ImageArchiveMetadata.ImageType getType() {
            return type;
        }
    }
}
