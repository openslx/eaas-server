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

package de.bwl.bwfla.blobstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bwl.bwfla.blobstore.api.Blob;
import de.bwl.bwfla.blobstore.api.BlobDescription;

import javax.persistence.Entity;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


@Entity
public class BlobStoreEntry
{
	@JsonProperty(value = "namespace", required = true)
	private String namespace;

	@JsonProperty(value = "id", required = true)
	private String id;

	@JsonProperty(value = "access_token", required = true)
	private String accessToken;

	@JsonProperty(value = "type", required = true)
	private String type;

	@JsonProperty("name")
	private String name;

	@JsonProperty(value = "description")
	private String description;


	public BlobStoreEntry()
	{
		this.namespace = null;
		this.id = null;
		this.accessToken = null;
		this.type = null;
		this.name = null;
		this.description = null;
	}

	public BlobStoreEntry(String namespace, String id, String token, String type)
	{
		this.setNamespace(namespace);
		this.setId(id);
		this.setAccessToken(token);
		this.setType(type);

		this.name = null;
		this.description = null;
	}

	public BlobStoreEntry(String id, BlobDescription blob)
	{
		this.setNamespace(blob.getNamespace());
		this.setId(id);
		this.setAccessToken(blob.getAccessToken());
		this.setType(blob.getType());

		if (blob.hasName())
			this.setName(blob.getName());

		if (blob.hasDescription())
			this.setDescription(blob.getDescription());
	}

	public String getNamespace()
	{
		return namespace;
	}

	public BlobStoreEntry setNamespace(String namespace)
	{
		Blob.checkNamespace(namespace);
		this.namespace = namespace;
		return this;
	}

	public String getId()
	{
		return id;
	}

	public BlobStoreEntry setId(String id)
	{
		Blob.checkId(id);
		this.id = id;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public BlobStoreEntry setType(String type)
	{
		Blob.checkType(type);
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

	public BlobStoreEntry setName(String name)
	{
		Blob.checkName(name);
		this.name = name;
		return this;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public BlobStoreEntry setAccessToken(String token)
	{
		Blob.check(token, "Blob's access token");
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

	public BlobStoreEntry setDescription(String description)
	{
		Blob.check(description, "Blob's description");
		this.description = description;
		return this;
	}

	public boolean accessTokenEquals(String token)
	{
		final String exptoken = this.getAccessToken();
		return BlobDescription.accessTokenEquals(token, exptoken);
	}

	public Blob toBlob()
	{
		final Blob blob = new Blob();
		blob.setId(this.getId())
				.setNamespace(this.getNamespace())
				.setAccessToken(this.getAccessToken())
				.setType(this.getType())
				.setName(this.getName())
				.setDescription(this.getDescription());

		return blob;
	}


	public static void write(BlobStoreEntry entry, Writer writer) throws IOException
	{
		final ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter()
				.writeValue(writer, entry);
	}

	public static BlobStoreEntry read(Reader reader) throws IOException
	{
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(reader, BlobStoreEntry.class);
	}

	public static BlobStoreEntry read(Path path) throws IOException
	{
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			return BlobStoreEntry.read(reader);
		}
	}
}
