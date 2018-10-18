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

package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SessionResource extends JaxbType
{
	@XmlType
	@XmlEnum
	public enum Type
	{
		@XmlEnumValue("component")
		COMPONENT,

		@XmlEnumValue("network")
		NETWORK
	}


	@XmlElement(required = true)
	private String id;

	@XmlElement(required = true)
	private Type type;

	@XmlElement(required = true, name = "keepalive_url")
	private String keepaliveUrl;

	@XmlTransient
	private boolean failed = false;


	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public String getKeepaliveUrl()
	{
		return keepaliveUrl;
	}

	public void setKeepaliveUrl(String url)
	{
		this.keepaliveUrl = url;
	}

	public boolean isFailed()
	{
		return failed;
	}

	public void setFailed(boolean failed)
	{
		this.failed = failed;
	}
}
