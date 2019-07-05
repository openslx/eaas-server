package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "emulationEnvironmentTemplate", namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlRootElement(name="emulationEnvironmentTemplate", namespace = "http://bwfla.bwl.de/common/datatypes")
public class MachineConfigurationTemplate extends  MachineConfiguration
{

	@XmlElement(required = false, defaultValue = "0")
	private int templateVersion;

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
	private ImageGeneralization imageGeneralization;

	public ImageGeneralization getImageGeneralization()
	{
		return imageGeneralization;
	}


	public static MachineConfigurationTemplate fromValue(String data) throws JAXBException {
		return JaxbType.fromValue(data, MachineConfigurationTemplate.class);
	}

	@Override
	public MachineConfigurationTemplate copy() {
		try {
			return MachineConfigurationTemplate.fromValue(this.value());
		} catch (JAXBException e) {
			Logger.getLogger(MachineConfigurationTemplate.class.getName()).log(Level.WARNING, e.getMessage(), e);
			return null;
		}
	}

	public MachineConfiguration implement() throws JAXBException {

		MachineConfiguration m = new MachineConfiguration(this);
		
		JAXBContext jc = JAXBContext.newInstance(MachineConfiguration.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter w = new StringWriter();
		marshaller.marshal(m, w);
		return MachineConfiguration.fromValue(w.toString());
	}
}
