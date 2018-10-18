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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BlobHandle
{
	@XmlElement(name = "namespace", required = true)
	private String namespace;

	@XmlElement(name = "id", required = true)
	private String id;

	@XmlElement(name = "access_token", required = true)
	private String accessToken;

	public BlobHandle()
	{
		this.namespace = "";
		this.id = "";
		this.accessToken = "";
	}

	public BlobHandle(String namespace, String id, String accessToken)
	{
		this.setNamespace(namespace);
		this.setId(id);
		this.setAccessToken(accessToken);
	}

	public String getNamespace()
	{
		return namespace;
	}

	public BlobHandle setNamespace(String namespace)
	{
		Blob.checkNamespace(namespace);
		this.namespace = namespace;
		return this;
	}

	public String getId()
	{
		return id;
	}

	public BlobHandle setId(String id)
	{
		Blob.checkId(id);
		this.id = id;
		return this;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public BlobHandle setAccessToken(String token)
	{
		Blob.checkAccessToken(token);
		this.accessToken = token;
		return this;
	}

	public boolean isValid()
	{
		return !(namespace.isEmpty() || id.isEmpty() || accessToken.isEmpty());
	}

	public String toRestUrl(String address)
	{
		return this.toRestUrl(address, true);
	}

	public String toRestUrl(String address, boolean withAccessToken)
	{
		String url = address + '/' + namespace + '/' + id;
		if (withAccessToken)
			url += "?access_token=" + accessToken;

		return url;
	}

	public static BlobHandle fromUrl(String resturl) throws MalformedURLException
	{
		String[] segments = resturl.split("/");
		String namespace = segments[segments.length-2];
		String id = segments[segments.length-1];
		return new BlobHandle(namespace, id, BlobDescription.DEFAULT_ACCESSTOKEN);
	}
}
