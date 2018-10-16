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

        @XmlMimeType("application/octet-stream")
        @XmlElement
        DataHandler fileHandle;

        @XmlElement
        String id;

        @XmlElement
        ImageArchiveMetadata.ImageType type;

        ImageFileInfo() {}

        public ImageFileInfo(@XmlMimeType("application/octet-stream")DataHandler fileHandle, String id, ImageArchiveMetadata.ImageType type)
        {
            this.fileHandle = fileHandle;
            this.id = id;
            this.type = type;
        }

        public DataHandler getFileHandle() {
            return fileHandle;
        }

        public String getId() {
            return id;
        }

        public ImageArchiveMetadata.ImageType getType() {
            return type;
        }
    }
}
