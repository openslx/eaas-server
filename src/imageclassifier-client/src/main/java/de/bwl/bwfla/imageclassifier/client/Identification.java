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

import de.bwl.bwfla.common.datatypes.identification.DiskType;
import de.bwl.bwfla.emucomp.api.FileCollection;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlSeeAlso({ClassificationEntry.class, HistogramEntry.class})
public class Identification<T>
{
    public final static String MEDIATYPE_CLASSIFICATION_AS_JSON = "application/vnd.bwfla.imageclassifier.classification.v2+json";
    public final static String MEDIATYPE_CLASSIFICATION_AS_XML = "application/vnd.bwfla.imageclassifier.classification.v2+xml";
    public final static String MEDIATYPE_HISTOGRAM_AS_JSON = "application/vnd.bwfla.imageclassifier.histogram.v2+json";
    public final static String MEDIATYPE_HISTOGRAM_AS_XML = "application/vnd.bwfla.imageclassifier.histogram.v2+xml";

    @XmlElement
	private FileCollection fileCollection;

    @XmlElement
	private HashMap<String, IdentificationDetails<T>> identificationData;

    @XmlElement
	private String url;

    @XmlElement
	private String filename;

	Identification() {}

	public Identification(FileCollection fc, HashMap<String, IdentificationDetails<T>> data)
	{
		this.fileCollection = fc;
		identificationData = data;
	}

	public Identification(String filename, String url,  HashMap<String, IdentificationDetails<T>> data)
	{
		this.url = url;
		this.filename = filename;
		this.identificationData = data;
	}

	public FileCollection getFileCollection() {
		return fileCollection;
	}

	public HashMap<String, IdentificationDetails<T>> getIdentificationData() {
		return identificationData;
	}

	public String getUrl() {
		return url;
	}

	public String getFilename() {
		return filename;
	}

	public static class IdentificationDetails<T>
	{
		@XmlElement(name="entries")
		private List<T> entries;

		@XmlElement(name = "disktype")
		private  DiskType diskType;

		public List<T> getEntries() {
			return entries;
		}

		public void setEntries(List<T> entries) {
			this.entries = entries;
		}

		public DiskType getDiskType() {
			return diskType;
		}

		public void setDiskType(DiskType diskType) {
			this.diskType = diskType;
		}
	}
}
