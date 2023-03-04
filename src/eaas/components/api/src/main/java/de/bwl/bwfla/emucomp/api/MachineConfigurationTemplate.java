package de.bwl.bwfla.emucomp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="emulationEnvironmentTemplate", namespace = "http://bwfla.bwl.de/common/datatypes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MachineConfigurationTemplate extends  MachineConfiguration
{
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
}
