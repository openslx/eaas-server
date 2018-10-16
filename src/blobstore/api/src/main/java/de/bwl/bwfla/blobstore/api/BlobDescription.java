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
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.nio.file.Path;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BlobDescription
{
	@XmlElement(name = "namespace", required = true)
	private String namespace;

	@XmlElement(name = "accessToken", required = true)
	private String accessToken;

	@XmlElement(name = "type", required = true)
	private String type;

	@XmlElement(name = "name")
	private String name;

	@XmlElement(name = "description")
	private String description;

	@XmlElement(name = "data", required = true)
	private @XmlMimeType("application/octet-stream") DataHandler data;

	/** Pattern representing valid names and namespaces */
	private static final String NAME_PATTERN = "\\w(-|\\w)*";

	/** Pattern representing valid types */
	private static final String TYPE_PATTERN = "(\\.[a-zA-Z0-9]+)+";

	/** Default access token value */
	public static final String DEFAULT_ACCESSTOKEN = "default";


	public BlobDescription()
	{
		this.namespace = null;
		this.type = null;
		this.name = null;
		this.accessToken = DEFAULT_ACCESSTOKEN;
		this.description = null;
		this.data = null;
	}

	public BlobDescription(String type, DataHandler data)
	{
		this.setType(type);
		this.setData(data);

		this.namespace = null;
		this.accessToken = null;
		this.name = null;
		this.description = null;
	}

	public BlobDescription(String namespace, String token, String type, DataHandler data)
	{
		this.setNamespace(namespace);
		this.setAccessToken(token);
		this.setType(type);
		this.setData(data);

		this.name = null;
		this.description = null;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public BlobDescription setNamespace(String namespace)
	{
		BlobDescription.checkNamespace(namespace);
		this.namespace = namespace;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public BlobDescription setType(String type)
	{
		BlobDescription.checkType(type);
		this.type = type;
		return this;
	}

	public boolean hasName()
	{
		return (name != null && !name.isEmpty());
	}

	public String getName()
	{
		return name;
	}

	public BlobDescription setName(String name)
	{
		BlobDescription.checkName(name);
		this.name = name;
		return this;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public BlobDescription setAccessToken(String token)
	{
		BlobDescription.checkAccessToken(token);
		this.accessToken = token;
		return this;
	}

	public boolean hasDescription()
	{
		return (description != null && !description.isEmpty());
	}

	public String getDescription()
	{
		return description;
	}

	public BlobDescription setDescription(String description)
	{
		BlobDescription.check(description, "Blob's description");
		this.description = description;
		return this;
	}

	public DataHandler getData()
	{
		return data;
	}

	public BlobDescription setData(DataHandler data)
	{
		if (data == null)
			throw new IllegalArgumentException("Blob's data is null!");

		this.data = data;
		return this;
	}

	public BlobDescription setData(DataSource source)
	{
		if (source == null)
			throw new IllegalArgumentException("Blob's data source is null!");

		return this.setData(new DataHandler(source));
	}

	public BlobDescription setDataFromFile(Path path)
	{
		if (path == null)
			throw new IllegalArgumentException("Blob's data path is null!");

		return this.setData(new FileDataSource(path.toFile()));
	}

	public BlobDescription setDataFromUrl(URL url)
	{
		if (url == null)
			throw new IllegalArgumentException("Blob's data URL is null!");

		return this.setData(new URLDataSource(url));
	}

	public boolean accessTokenEquals(String token)
	{
		final String exptoken = this.getAccessToken();
		return BlobDescription.accessTokenEquals(token, exptoken);
	}


	/* =============== Public Utils =============== */

	public static void check(String value, String prefix)
	{
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(prefix + " is null or empty!");
	}

	public static void checkNamespace(String namespace)
	{
		BlobDescription.check(namespace, "Blob's namespace");
		if (!namespace.matches(NAME_PATTERN))
			throw new IllegalArgumentException("Blob's namespace contains invalid character(s)!");
	}

	public static void checkAccessToken(String token)
	{
		BlobDescription.check(token, "Blob's access token");
		if (!token.matches(NAME_PATTERN))
			throw new IllegalArgumentException("Blob's access token contains invalid character(s)!");
	}

	public static void checkType(String name)
	{
		BlobDescription.check(name, "Blob's type");
		if (!name.matches(TYPE_PATTERN))
			throw new IllegalArgumentException("Blob's type contains invalid character(s)!");
	}

	public static void checkName(String name)
	{
		BlobDescription.check(name, "Blob's name");
		if (!name.matches(NAME_PATTERN))
			throw new IllegalArgumentException("Blob's name contains invalid character(s)!");
	}

	public static boolean accessTokenEquals(String token, String exptoken)
	{
		// Nothing to compare?
		if (exptoken == null)
			return (token == null || token.isEmpty());

		// A non-null token is expected!
		if (token == null)
			return false;

		return exptoken.contentEquals(token);
	}
}
