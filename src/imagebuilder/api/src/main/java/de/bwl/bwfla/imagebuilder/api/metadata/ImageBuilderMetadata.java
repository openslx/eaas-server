package de.bwl.bwfla.imagebuilder.api.metadata;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({
  DockerImport.class,
})
public abstract class ImageBuilderMetadata extends JaxbType{
}
