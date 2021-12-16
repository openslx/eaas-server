/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.emucomp.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "containerConfiguration", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
		"inputs",
		"output",
		"input",
		"dataResources",
		"digest"
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "configurationType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = DockerContainerConfiguration.class, name = ContainerConfiguration.Names.DOCKER),
		@JsonSubTypes.Type(value = OciContainerConfiguration.class, name = ContainerConfiguration.Names.OCI)
})
public class ContainerConfiguration extends Environment
{
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", name = "inputs", required = false)
	protected List<Input> inputs = new ArrayList<Input>();

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
	protected String output;

	@XmlElement
	protected String digest;


	@XmlElementRefs({
			@XmlElementRef(name="binding", type=Binding.class, namespace = "http://bwfla.bwl.de/common/datatypes"),
			@XmlElementRef(name="objectArchiveBinding", type=ObjectArchiveBinding.class, namespace = "http://bwfla.bwl.de/common/datatypes")
	})
	protected List<AbstractDataResource> dataResources = new ArrayList<AbstractDataResource>();

	@XmlElement
	private String input;

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	public String getInput() {
		return input;
	}

	public void setInputPath(String input) {
		this.input = input;
	}



	public boolean hasInputs()
	{
		return (inputs != null && !inputs.isEmpty());
	}

	public List<Input> getInputs()
	{
		return inputs;
	}

	public void setInputs(List<Input> inputs)
	{
		this.inputs = inputs;
	}

	public boolean hasOutputPath()
	{
		return (output != null && !output.isEmpty());
	}

	public String getOutputPath()
	{
		return output;
	}

	public void setOutputPath(String path)
	{
		this.output = path;
	}

	public List<AbstractDataResource> getDataResources()
	{
		return dataResources;
	}

	public void setDataResources(List<AbstractDataResource> resources)
	{
		this.dataResources = resources;
	}

	public static ContainerConfiguration fromValue(String data) throws JAXBException
	{
		return JaxbType.fromValue(data, ContainerConfiguration.class);
	}


	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "input", propOrder = {
			"binding",
			"destination"
	})
	public static class Input
	{
		@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
		protected String binding;

		@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
		protected String destination;


		public Input setBinding(String binding)
		{
			this.binding = binding;
			return this;
		}

		public String getBinding()
		{
			return binding;
		}

		public Input setDestination(String path)
		{
			this.destination = path;
			return this;
		}

		public String getDestination()
		{
			return destination;
		}
	}


	/** Class names to use as type-information (compile-time constants) */
	public static class Names
	{
		public static final String DOCKER = "de.bwl.bwfla.emucomp.api.DockerContainerConfiguration";
		public static final String OCI    = "de.bwl.bwfla.emucomp.api.OciContainerConfiguration";
	}
}
