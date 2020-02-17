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

package de.bwl.bwfla.emucomp.api;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.nio.file.Path;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "bindingDataHandler", namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class BindingDataHandler
{
	@XmlAttribute(name = "id")
	protected String id;

	@XmlElement(name = "data", required = true)
	private @XmlMimeType("application/octet-stream") DataHandler data;

	@XmlElement
	private String url;

	/** Returns the ID of the binding. */
	public String getId()
	{
		return id;
	}

	/** Sets the ID of the binding. */
	public BindingDataHandler setId(String value)
	{
		this.id = value;
		return this;
	}

	public DataHandler getData()
	{
		return data;
	}

	public BindingDataHandler setData(DataHandler data)
	{
		if (data == null)
			throw new IllegalArgumentException("Bindings's data is null!");

		this.data = data;
		return this;
	}

	public BindingDataHandler setData(DataSource source)
	{
		if (source == null)
			throw new IllegalArgumentException("Bindings's data source is null!");

		return this.setData(new DataHandler(source));
	}

	public BindingDataHandler setDataFromFile(Path path)
	{
		if (path == null)
			throw new IllegalArgumentException("Bindings's data path is null!");

		return this.setData(new FileDataSource(path.toFile()));
	}


	public String getUrl() {
		return url;
	}

	public BindingDataHandler setUrl(String url) {
		this.url = url;
		return this;
	}
}
