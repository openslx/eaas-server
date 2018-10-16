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

package de.bwl.bwfla.imageproposer.client;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Proposal
{
    public static final String MEDIATYPE_AS_JSON = "application/vnd.bwfla.imageproposer.proposal.v2+json";
    public static final String MEDIATYPE_AS_XML = "application/vnd.bwfla.imageproposer.proposal.v2+xml";

	@XmlElement(name="images")
	private Collection<String> images;
	
	@XmlElement(name="suggested")
	private Map<String, String> suggested;

	public Proposal()
	{
	}
	
	public Proposal(Collection<String> images, Map<String, String> missing)
	{
		this.images = images;
		this.suggested = missing;
	}

	public Collection<String> getImages()
	{
		return images;
	}
	
	public void setImages(Collection<String> images)
	{
		this.images = images;
	}
	
	public Map<String, String> getSuggested() {
		return suggested;
	}

	public void setSuggested(Map <String, String> suggested) {
		this.suggested = suggested;
	}

	public class OperatingSystem
	{

		@XmlElement(name="id")
		String id;
		@XmlElement(name="label")
		String label;

		public OperatingSystem(String id, String label)
		{
			this.id = id;
			this.label = label;
		}

	}
}
