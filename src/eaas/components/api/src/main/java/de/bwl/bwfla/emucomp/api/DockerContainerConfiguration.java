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

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.persistence.Transient;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.logging.Level;
import java.util.logging.Logger;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dockerContainerConfiguration", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
		"image",
})
@XmlRootElement(name = "dockerContainerConfiguration", namespace = "http://bwfla.bwl.de/common/datatypes")
public class DockerContainerConfiguration extends ContainerConfiguration
{
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
	protected String image;

	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	public static DockerContainerConfiguration fromValue(String data) throws JAXBException
	{
		return JaxbType.fromValue(data, DockerContainerConfiguration.class);
	}

	public DockerContainerConfiguration copy()
	{
		try {
			return DockerContainerConfiguration.fromValue(this.value());
		}
		catch (JAXBException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}
}
