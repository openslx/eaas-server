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
import de.bwl.bwfla.emucomp.api.MediumType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Map;


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

	/** @documentationExample env-id-3 */
	@XmlElement(name="environments")
	private Collection<String> environments;
	
	@XmlElement(name="suggested")
	private Map<String, String> suggested;


	public Proposal()
	{
	}
	
	public Proposal(String imageurl, MediumType imagetype, Collection<String> environments, Map<String, String> suggested)
	{
		this.imageurl = imageurl;
		this.imagetype = imagetype;
		this.environments = environments;
		this.suggested = suggested;
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

	public Proposal setEnvironments(Collection<String> environments)
	{
		this.environments = environments;
		return this;
	}

	public Collection<String> getEnvironments()
	{
		return environments;
	}

	public Proposal setSuggested(Map <String, String> suggested)
	{
		this.suggested = suggested;
		return this;
	}

	public Map<String, String> getSuggested()
	{
		return suggested;
	}
}
