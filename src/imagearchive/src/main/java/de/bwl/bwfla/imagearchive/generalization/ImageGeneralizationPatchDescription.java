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

package de.bwl.bwfla.imagearchive.generalization;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "imageGeneralizationPatchDescription", namespace = "http://bwfla.bwl.de/common/datatypes")
public class ImageGeneralizationPatchDescription extends JaxbType
{
	@XmlElement(name = "name", namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
	private String name;

	@XmlElement(name = "description", namespace = "http://bwfla.bwl.de/common/datatypes")
	private String description;


	public ImageGeneralizationPatchDescription()
	{
		// Empty!
	}

	public ImageGeneralizationPatchDescription(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

	public static ImageGeneralizationPatchDescription fromValue(String data) throws JAXBException
	{
		return JaxbType.fromValue(data, ImageGeneralizationPatchDescription.class);
	}
}
