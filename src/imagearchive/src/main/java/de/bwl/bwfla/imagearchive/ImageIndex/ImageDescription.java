package de.bwl.bwfla.imagearchive.ImageIndex;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.imagearchive.ImageIndex.ImageNameIndex;
import org.apache.tamaya.Configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageDescription extends JaxbType
{
    @XmlElement
    private  String url = "";
    @XmlElement
    private  String id = "";
    @XmlElement
    private  String type = "";
    @XmlElement
    private  String fstype = "";

    ImageDescription(String url, String id, String type, String fstype)
    {
        this.url = url;
        this.id = id;
        this.type = type;
        this.fstype = fstype;
    }

    ImageDescription(String prefix, Configuration values, Configuration defaults)
    {
        this.url = ImageNameIndex.getOrDefault(prefix + "url", values, defaults);
        this.id = ImageNameIndex.getOrDefault(prefix + "id", values, defaults, true);
        this.type = ImageNameIndex.getOrDefault(prefix + "type", values, defaults, true);
        this.fstype = ImageNameIndex.getOrDefault(prefix + "fstype", values, defaults);
    }
    ImageDescription()
    {
    }

    public String url()
    {
        return url;
    }



    public String id()
    {
        return id;
    }

    public String type()
    {
        return type;
    }

    public String fstype()
    {
        return fstype;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getFstype() {
        return fstype;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFstype(String fstype) {
        this.fstype = fstype;
    }
}
