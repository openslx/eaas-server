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

package de.bwl.bwfla.objectarchive.api;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SeatDescription extends JaxbType
{
	@XmlElement(name = Fields.RESOURCE)
	private String resource;

	@XmlElement(name = Fields.SEATS)
	private int seats;

	protected SeatDescription()
	{
		// Empty!
	}

	public SeatDescription(String resource, int seats)
	{
		this.resource = resource;
		this.seats = seats;
	}

	public String getResource()
	{
		return resource;
	}

	public int getNumSeats()
	{
		return seats;
	}

	private static final class Fields
	{
		public static final String RESOURCE  = "resource";
		public static final String SEATS     = "seats";
	}
}
