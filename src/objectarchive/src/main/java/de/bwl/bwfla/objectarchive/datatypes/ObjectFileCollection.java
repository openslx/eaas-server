package de.bwl.bwfla.objectarchive.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emucomp.api.Binding;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ObjectFileCollection extends JaxbType {

    @XmlElement
    List<ObjectFileCollectionHandle> files;

    @XmlElement
    String id;

    ObjectFileCollection() {}

    public ObjectFileCollection(String id)
    {
        this.id = id;
    }

    public List<ObjectFileCollectionHandle> getFiles() {
        return files;
    }

    public void setFiles(List<ObjectFileCollectionHandle> files) {
        this.files = files;
    }

    public String getId() {
        return id;
    }


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    static public class ObjectFileCollectionHandle {

        @XmlElement
        @XmlMimeType("application/octet-stream")DataHandler handle;

        @XmlElement
        Binding.ResourceType type;

        @XmlElement
        String filename;

        public ObjectFileCollectionHandle(@XmlMimeType("application/octet-stream")DataHandler handle, Binding.ResourceType rt, String filename)
        {
            this.handle = handle;
            this.type = rt;
            this.filename = filename;
        }

        ObjectFileCollectionHandle() {}

        public Binding.ResourceType getType() {
            return type;
        }

        public String getFilename() {
            return filename;
        }

        public DataHandler getHandle() {
            return handle;
        }
    }
}
