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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bwl.bwfla.emil.datatypes.rest.ClassificationResult;
import de.bwl.bwfla.emucomp.api.MediumType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Proposal
{
	/** @documentationExample https://emulation.cloud/blobs/image-123 */
	@XmlElement(name="image_url")
	private String imageurl;

	/** @documentationExample cdrom */
	@XmlElement(name="image_type")
	private MediumType imagetype;

	@XmlElement(name="result")
	private ClassificationResult result;


	public Proposal()
	{
	}
	
	public Proposal(String imageurl, MediumType imagetype, ClassificationResult result)
	{
		this.imageurl = imageurl;
		this.imagetype = imagetype;
		this.result = result;
	}

	public Proposal setImportedImageUrl(String url)
	{
		this.imageurl = url;
		return this;
	}

	@JsonIgnore
	public String getImportedImageUrl()
	{
		return imageurl;
	}

	public Proposal setImportedImageType(MediumType type)
	{
		this.imagetype = type;
		return this;
	}

	@JsonIgnore
	public MediumType getImportedImageType()
	{
		return imagetype;
	}

	public Proposal setResult(ClassificationResult result)
	{
		this.result = result;
		return this;
	}

	public ClassificationResult getResult()
	{
		return result;
	}
}
