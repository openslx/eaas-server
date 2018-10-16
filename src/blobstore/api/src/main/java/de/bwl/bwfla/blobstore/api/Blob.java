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

package de.bwl.bwfla.blobstore.api;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Blob extends BlobDescription
{
	@XmlElement(name = "id", required = true)
	private String id;

	@XmlElement(name = "size")
	private long size;

	@XmlElement(name = "creation_timestamp")
	private long creationTimestamp;


	public Blob()
	{
		super();

		this.id = null;
		this.size = 0;
		this.creationTimestamp = System.currentTimeMillis();
	}

	public Blob(String type, DataHandler data)
	{
		super(type, data);

		this.id = null;
		this.size = 0;
		this.creationTimestamp = System.currentTimeMillis();
	}

	public Blob(String id, String namespace, String token, String type, DataHandler data)
	{
		super(namespace, token, type, data);

		this.size = 0;
		this.setId(id);
		this.creationTimestamp = System.currentTimeMillis();
	}

	public String getId()
	{
		return id;
	}

	public Blob setId(String id)
	{
		Blob.checkId(id);
		this.id = id;
		return this;
	}

	public long getSize()
	{
		return size;
	}

	public Blob setSize(long size)
	{
		if (size < 1)
			throw new IllegalArgumentException("Invalid blob's size!");

		this.size = size;
		return this;
	}

	public long getCreationTimestamp()
	{
		return creationTimestamp;
	}


	/* =============== Public Utils =============== */

	public static void checkId(String id)
	{
		BlobDescription.check(id, "Blob's ID");
	}
}
