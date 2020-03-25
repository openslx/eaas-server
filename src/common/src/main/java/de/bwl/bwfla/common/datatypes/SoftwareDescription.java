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

package de.bwl.bwfla.common.datatypes;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "softwareDescription", namespace="http://bwfla.bwl.de/common/datatypes")
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class SoftwareDescription extends JaxbType
{
	@XmlElement(required = true)
	private String softwareId;

	@XmlElement(required = true)
	private String label;
	
	@XmlElement(required = false)
	private boolean isOperatingSystem = false;

	@XmlElement(required = false)
	private boolean isPublic = false;


	@XmlElement(required = true, defaultValue = "default")
	private String archiveId;

	public SoftwareDescription()
	{
		this(null, null, false, null);
	}
	
	public SoftwareDescription(String id, String label, boolean isOs, String archiveId)
	{
		this.softwareId = id;
		this.label = label;
		this.isOperatingSystem = isOs;
		this.archiveId = archiveId;
	}
	
	public String getSoftwareId()
	{
		return softwareId;
	}
	
	public String getLabel()
	{
		return label;
	}

	public void setSoftwareId(String id)
	{
		this.softwareId = id;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public boolean getIsOperatingSystem() {
		return isOperatingSystem;
	}

	public void setIsOperatingSystem(boolean isOperatingSystem) {
		this.isOperatingSystem = isOperatingSystem;
	}

	public String getArchiveId() {
		return archiveId;
	}

	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean aPublic) {
		isPublic = aPublic;
	}
}
