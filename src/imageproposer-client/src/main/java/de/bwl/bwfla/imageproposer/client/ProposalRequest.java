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

import de.bwl.bwfla.common.datatypes.identification.DiskType;
import de.bwl.bwfla.emucomp.api.FileCollection;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ProposalRequest
{
    public static final String MEDIATYPE_AS_JSON = "application/vnd.bwfla.imageproposer.request.v2+json";
    public static final String MEDIATYPE_AS_XML = "application/vnd.bwfla.imageproposer.request.v2+xml";

	@XmlElement(name="fileFormats")
	private HashMap<String, List<Entry>> fileFormats;

	@XmlElement(name="mediaFormats")
	private HashMap<String, DiskType> mediaFormats;

	public ProposalRequest(HashMap<String, List<Entry>> fileFormats, HashMap<String, DiskType> mediaFormats)
	{
		this.fileFormats = fileFormats;
		this.mediaFormats = mediaFormats;
	}

	ProposalRequest() {}

	public HashMap<String, List<Entry>> getFileFormats() {
		return fileFormats;
	}

	public void setFileFormats(HashMap<String, List<Entry>> fileFormats) {
		this.fileFormats = fileFormats;
	}

	public HashMap<String, DiskType> getMediaFormats() {
		return mediaFormats;
	}

	public void setMediaFormats(HashMap<String, DiskType> mediaFormats) {
		this.mediaFormats = mediaFormats;
	}


	@XmlRootElement
	public static class Entry
	{
		private String type;
		private int count;
		
        public Entry() {
        }

        public Entry(String type, int count) {
            this.type = type;
            this.count = count;
        }
            
		public String getType()
		{
			return type;
		}
		
		public void setType(String type)
		{
			this.type = type;
		}
		
		public int getCount()
		{
			return count;
		}
		
		public void setCount(int count)
		{
			this.count = count;
		}
	}
}
