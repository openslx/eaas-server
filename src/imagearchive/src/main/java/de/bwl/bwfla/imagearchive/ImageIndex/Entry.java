package de.bwl.bwfla.imagearchive.ImageIndex;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import org.apache.tamaya.Configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Entry extends JaxbType
{
    @XmlElement
    private  String name = "";

    @XmlElement
    private  String version  = "";

    @XmlElement
    private ImageDescription image = null;

    @XmlElement
    private Provenance provenance;

    @XmlElement
    private String digest;

    Entry(String name, String version, ImageDescription image)
    {
        this.name = name;
        this.version = version;
        this.image = image;
    }

    Entry(Configuration values, Configuration defaults)
    {
        this.name = ImageNameIndex.getOrDefault("name", values, defaults, true);
        this.version = ImageNameIndex.getOrDefault("version", values, defaults);
        this.image = new ImageDescription("image.", values, defaults);
    }

    private Entry()
    {
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }


    public String name()
    {
        return name;
    }

    public String version()
    {
        return version;
    }

    public Provenance getProvenance() {
        return provenance;
    }

    public void setProvenance(Provenance provenance) {
        this.provenance = provenance;
    }

    public ImageDescription getImage() {
        return image;
    }

    public void setImage(ImageDescription image) {
        this.image = image;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }
}
