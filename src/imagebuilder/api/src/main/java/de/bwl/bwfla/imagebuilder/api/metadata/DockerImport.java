package de.bwl.bwfla.imagebuilder.api.metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DockerImport extends ImageBuilderMetadata {

    @XmlElement
    private String imageRef;

    @XmlElement
    private String tag;

    @XmlElement
    private List<String> layers;

    public String getImageRef() {
        return imageRef;
    }

    public void setImageRef(String imageRef) {
        this.imageRef = imageRef;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getLayers() {
        return layers;
    }

    public void setLayers(List layers) {
        this.layers = layers;
    }
}
