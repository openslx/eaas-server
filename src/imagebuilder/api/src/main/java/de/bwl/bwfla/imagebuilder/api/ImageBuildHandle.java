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

package de.bwl.bwfla.imagebuilder.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageBuildHandle
{
	@XmlElement(required = true)
	private String id;


	public ImageBuildHandle()
	{
		this.id = null;
	}

	public ImageBuildHandle(String id)
	{
		this.setId(id);
	}

	public String getId()
	{
		return id;
	}

	public ImageBuildHandle setId(String id)
	{
		if (id == null || id.isEmpty())
			throw new IllegalArgumentException("ID is invalid!");

		this.id = id;
		return this;
	}
}
