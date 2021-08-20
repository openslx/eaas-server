package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageModificationCondition {
    @XmlElement
    private String partitionName;
    @XmlElement
    private String fsType;
    @XmlElement
    private Collection<String> paths;

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partname) {
        this.partitionName = partname;
    }

    public String getFstype() {
        return fsType;
    }

    public void setFstype(String fstype) {
        this.fsType = fstype;
    }

    public Collection<String> getPaths() {
        if(paths == null)
            paths = new ArrayList<>();
        return paths;
    }

    public void setPaths(Collection<String> paths) {
        this.paths = paths;
    }
}
