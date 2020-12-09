package de.bwl.bwfla.imagearchive.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageModificationCondition {
    @XmlElement
    private String partname;
    @XmlElement
    private String fstype;
    @XmlElement
    private Collection<String> paths;

    public String getPartname() {
        return partname;
    }

    public void setPartname(String partname) {
        this.partname = partname;
    }

    public String getFstype() {
        return fstype;
    }

    public void setFstype(String fstype) {
        this.fstype = fstype;
    }

    public Collection<String> getPaths() {
        return paths;
    }

    public void setPaths(Collection<String> paths) {
        this.paths = paths;
    }
}
