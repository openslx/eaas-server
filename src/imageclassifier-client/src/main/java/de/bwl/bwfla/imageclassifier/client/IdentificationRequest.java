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

package de.bwl.bwfla.imageclassifier.client;

import de.bwl.bwfla.emucomp.api.FileCollection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class IdentificationRequest
{
    public static final String MEDIATYPE_AS_JSON = "application/vnd.bwfla.imageclassifier.request.v1+json";
    public static final String MEDIATYPE_AS_XML = "application/vnd.bwfla.imageclassifier.request.v1+xml";
    
	@XmlElement(name="fileCollection")
	private FileCollection fileCollection;
	
	@XmlElement(name="policy_url")
	private String policyUrl;

	public IdentificationRequest()
	{
		this.fileCollection = null;
		this.policyUrl = null;
	}
	
	public IdentificationRequest(FileCollection fc, String policyUrl)
	{
		this.fileCollection = fc;
		this.policyUrl = policyUrl;
	}
	
	public String getPolicyUrl()
	{
		return policyUrl;
	}

	public void setPolicyUrl(String url)
	{
		this.policyUrl = url;
	}


	public FileCollection getFileCollection() {
		return fileCollection;
	}

	public void setFileCollection(FileCollection fileCollection) {
		this.fileCollection = fileCollection;
	}
}
