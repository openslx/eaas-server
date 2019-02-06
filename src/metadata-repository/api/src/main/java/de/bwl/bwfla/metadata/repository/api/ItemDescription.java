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

package de.bwl.bwfla.metadata.repository.api;

import de.bwl.bwfla.metadata.repository.json.IJsonStreamable;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.ws.rs.core.Response;
import java.io.InputStream;


public class ItemDescription implements IJsonStreamable
{
	private ItemIdentifierDescription identifier;
	private String metadata;

	public ItemDescription(ItemIdentifierDescription id)
	{
		this.identifier = id;
		this.metadata = null;
	}

	public ItemIdentifierDescription getIdentifier()
	{
		return identifier;
	}

	public String getMetaData()
	{
		return metadata;
	}

	public ItemDescription setIdentifier(ItemIdentifierDescription id)
	{
		this.identifier = id;
		return this;
	}

	public ItemDescription setMetaData(String metadata)
	{
		this.metadata = metadata;
		return this;
	}

	public void write(JsonGenerator json, String objname) throws JsonException
	{
		if (objname != null)
			json.writeStartObject(objname);
		else json.writeStartObject();

		this.getIdentifier()
				.write(json, Fields.IDENTIFIER);

		json.write(Fields.METADATA, this.getMetaData());
		json.writeEnd();
	}

	public static ItemDescription from(JsonObject json) throws JsonException
	{
		try {
			final JsonObject idobj = json.getJsonObject(Fields.IDENTIFIER);
			return new ItemDescription(ItemIdentifierDescription.from(idobj))
					.setMetaData(json.getString(Fields.METADATA));
		}
		catch (Exception error) {
			throw new JsonException("Constructing object failed!", error);
		}
	}

	public static ItemDescription from(Response response) throws JsonException
	{
		final InputStream istream = response.readEntity(InputStream.class);
		try (final JsonParser parser = Json.createParser(istream)) {
			return ItemDescription.from(parser.getObject());
		}
	}


	// ========== Internal Helpers ==============================

	private static final class Fields
	{
		static final String IDENTIFIER = "identifier";
		static final String METADATA   = "metadata";
	}
}
