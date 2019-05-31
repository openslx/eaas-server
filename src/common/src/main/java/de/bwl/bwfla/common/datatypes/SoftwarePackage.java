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

package de.bwl.bwfla.common.datatypes;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;

import de.bwl.bwfla.common.utils.jaxb.JaxbValidator;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "softwarePackage", namespace="http://bwfla.bwl.de/common/datatypes",
	propOrder = { "name", "description", "releaseDate", "infoSource", "location",
	              "licence", "numSeats", "QID", "language", "documentation", "archive",
	              "objectId", "supportedFileFormats", "isOperatingSystem", "timestamp" })
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class SoftwarePackage
{
	private static final String ID_SEPARATOR = "/";

	@XmlElement(required = true)
	private String name;
	
	private String description;
	private Date releaseDate;
	private String infoSource;
	private String location;
	private String licence;
	private int numSeats;
	private String QID;
	private String language;
	private String documentation;
	private boolean isOperatingSystem = false;
	
	@XmlElement(required = true)
	private String archive;
	
	@XmlElement(required = true)
	private String objectId;

	/** List of supported document/file formats */
	@XmlElement(name = "supportedFileFormat")
	private List<String> supportedFileFormats;

	@XmlElement(name = "timestamp")
	protected String timestamp = Instant.now().toString();

	public String getId() {
		return this.getArchive() + ID_SEPARATOR + this.getObjectId();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public String getInfoSource() {
		return infoSource;
	}

	public String getLocation() {
		return location;
	}

	public String getLicence() {
		return licence;
	}
	
	public int getNumSeats() {
		return numSeats;
	}

	public String getLanguage() {
		return language;
	}

	public String getDocumentation() {
		return documentation;
	}

	public String getArchive() {
		return archive;
	}
	
	public String getObjectId() {
		return objectId;
	}

	public List<String> getSupportedFileFormats() {
		return supportedFileFormats;
	}

	public String getQID() {
		return QID;
	}

	public String getTimestamp() {
		return  timestamp;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public void setInfoSource(String infoSource) {
		this.infoSource = infoSource;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setLicence(String licence) {
		this.licence = licence;
	}

	public void setNumSeats(int numSeats) {
		this.numSeats = numSeats;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public void setArchive(String archive) {
		this.archive = archive;
	}
	
	public void setObjectId(String id) {
		this.objectId = id;
	}
	
	public void setSupportedFileFormats(List<String> formats) {
		if(formats == null)
		{
			this.supportedFileFormats = new ArrayList<String>();
			return;
		}
		this.supportedFileFormats = formats;
	}

	public void setQID(String QID) {
		this.QID = QID;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String value() throws JAXBException {
    	StringWriter writer = new StringWriter();
    	this.writeTo(writer);
    	return writer.toString();
    }
	
	public boolean writeTo(Writer writer) throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(this.getClass());
    	Marshaller marshaller = jc.createMarshaller();
    	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    	marshaller.marshal(this, writer);
    	return true;
    }
	
	public static SoftwarePackage fromValue(String data)
    {
		return SoftwarePackage.fromValue(new StringReader(data));
    }
	
	public static SoftwarePackage fromValue(Reader reader)
    {
		try {
			JAXBContext jc = JAXBContext.newInstance(SoftwarePackage.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			StreamSource stream = new StreamSource(reader);
			SoftwarePackage result = (SoftwarePackage) unmarshaller.unmarshal(stream);
			JaxbValidator.validate(result);
			return result;
		} catch (Throwable t) {
			throw new IllegalArgumentException("passed 'data' metadata cannot be parsed by 'JAX-B', check input contents");
		}
    }

	public boolean getIsOperatingSystem() {
		return isOperatingSystem;
	}

	public void setIsOperatingSystem(boolean isOperatingSystem) {
		this.isOperatingSystem = isOperatingSystem;
	}
}
