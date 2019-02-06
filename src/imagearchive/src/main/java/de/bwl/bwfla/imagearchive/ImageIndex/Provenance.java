package de.bwl.bwfla.imagearchive.ImageIndex;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Provenance extends JaxbType
{
    @XmlElement
    private String ociSourceUrl;

    @XmlElement
    private String versionTag;

    @XmlElement
    private List<String> layers;

    public String getOciSourceUrl() {
        return ociSourceUrl;
    }

    public void setOciSourceUrl(String ociSourceUrl) {
        this.ociSourceUrl = ociSourceUrl;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    public List<String> getLayers() {
        return layers;
    }

    public void setLayers(List<String> layers) {
        this.layers = layers;
    }
}