package de.bwl.bwfla.emil.datatypes.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bwl.bwfla.api.imagebuilder.ImageBuilderMetadata;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateContainerImageResult extends JaxbType {
    @XmlElement
    private String containerUrl;

    @XmlElement
    private ImageBuilderMetadata metadata;


    public String getContainerUrl() {
        return containerUrl;
    }

    public void setContainerUrl(String containerUrl) {
        this.containerUrl = containerUrl;
    }

    public ImageBuilderMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ImageBuilderMetadata metadata) {
        this.metadata = metadata;
    }
}
