package de.bwl.bwfla.imagearchive.ImageIndex;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import org.apache.tamaya.Configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageMetadata extends JaxbType
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

    public ImageMetadata(String name, ImageDescription image)
    {
        this.name = name;
        this.image = image;
    }

    private ImageMetadata()
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
        if(provenance == null)
            provenance = new Provenance();
        return provenance;
    }

    public ImageDescription getImage() {
        if(image == null)
            return new ImageDescription();
        return image;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }
}
