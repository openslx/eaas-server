package de.bwl.bwfla.emil.datatypes.rest;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.utils.Upload;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UploadResponse extends EmilResponseType {

    @XmlElement
    @Deprecated
    private List<String> uploads;

    @XmlElement
    private List<UploadedItem> uploadedItemList;

    public UploadResponse(BWFLAException e) {
        super(e);
    }

    public UploadResponse() {}

    @Deprecated
    public List<String> getUploads() {
        if(uploads == null)
            uploads = new ArrayList<>();

        return uploads;
    }

    public List<UploadedItem> getUploadedItemList() {
        if(uploadedItemList == null)
            uploadedItemList = new ArrayList<>();
        return uploadedItemList;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class UploadedItem  {
        @XmlElement
        private URL url;
        @XmlElement
        private String filename;

        UploadedItem() {}

        public UploadedItem(URL url, String filename)
        {
            this.filename = filename;
            this.url = url;
        }

        public URL getUrl() {
            return url;
        }


        public String getFilename() {
            return filename;
        }

    }

}
