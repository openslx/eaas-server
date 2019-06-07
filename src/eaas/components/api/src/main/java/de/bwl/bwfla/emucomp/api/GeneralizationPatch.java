package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generalizationPatch", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
        "imageGeneralization",
})
@XmlRootElement(name="generalizationPatch", namespace = "http://bwfla.bwl.de/common/datatypes")
public class GeneralizationPatch extends Environment {

    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
    private ImageGeneralization imageGeneralization;

    public ImageGeneralization getImageGeneralization()
    {
        return imageGeneralization;
    }

    public static GeneralizationPatch fromValue(String data) throws JAXBException {
        return JaxbType.fromValue(data, GeneralizationPatch.class);
    }
}
