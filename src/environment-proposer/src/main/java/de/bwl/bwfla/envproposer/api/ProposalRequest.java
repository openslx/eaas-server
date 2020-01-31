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

package de.bwl.bwfla.envproposer.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ProposalRequest
{
	@XmlElement(name="data_url", required = true)
	private String dataurl;

	@XmlElement(name="data_type", required = true)
	private DataType datatype;


	private ProposalRequest()
	{
		this.dataurl = null;
		this.datatype = null;
	}

	public ProposalRequest(String dataurl, DataType datatype)
	{
		this.dataurl = dataurl;
		this.datatype = datatype;
	}

	public ProposalRequest setDataUrl(String url)
	{
		this.dataurl = url;
		return this;
	}

	public String getDataUrl()
	{
		return dataurl;
	}

	public ProposalRequest setDataType(DataType type)
	{
		this.datatype = type;
		return this;
	}

	public DataType getDataType()
	{
		return datatype;
	}


	@XmlEnum
	public enum DataType
	{
		@XmlEnumValue("zip")
		ZIP,

		@XmlEnumValue("tar")
		TAR,

		@XmlEnumValue("bagit+zip")
		BAGIT_ZIP,

		@XmlEnumValue("bagit+tar")
		BAGIT_TAR,
	}
}
