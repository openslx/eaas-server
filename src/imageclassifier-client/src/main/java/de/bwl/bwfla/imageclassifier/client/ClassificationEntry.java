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

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class ClassificationEntry extends HistogramEntry
{
	@XmlElement(name="typeName")
	private String typeName;
    @XmlElement(name="files")
	private List<String> files;
    @XmlElement(name="read_qids")
	private List<String> readQID;
    @XmlElement(name="write_qids")
	private List<String> writeQID;
	@XmlElement(name="fromDate")
	private long fromDate;
	@XmlElement(name="toDate")
	private long toDate;

	public ClassificationEntry()
	{
		this(null, null, null);
	}

	public ClassificationEntry(String type, List<String> files)
	{
		this(type, null, files);
	}

	public ClassificationEntry(String type, String value, List<String> files)
	{
		super(type, (files != null) ? files.size() : 0, value);
		this.files = files;
		Date now = new Date();
		this.setFromDate(now.getTime());
		this.setToDate(now.getTime());
	}
	public ClassificationEntry(String type, String value, List<String> files, List<String> readQID, List<String> writeQID, String nameType)
	{
		super(type, (files != null) ? files.size() : 0, value);
		this.files = files;
		if(!readQID.isEmpty())
		    this.readQID = readQID;
		if(!writeQID.isEmpty())
			this.writeQID = writeQID;
		if(nameType != null)
			this.typeName = nameType;
	}

	public List<String> getFiles()
	{
		return files;
	}

	public void setFiles(List<String> files)
 	{
		this.setCount(files.size());
		this.files = files;
	}

	public String getTypeName() {
		return typeName;
	}

	public List<String> getReadQID() {
		return readQID;
	}

	public List<String> getWriteQID() {
		return writeQID;
	}

	public long getFromDate() {
		return fromDate;
	}

	public void setFromDate(long fromDate) {
		this.fromDate = fromDate;
	}

	public long getToDate() {
		return toDate;
	}

	public void setToDate(long toDate) {
		this.toDate = toDate;
	}
}
