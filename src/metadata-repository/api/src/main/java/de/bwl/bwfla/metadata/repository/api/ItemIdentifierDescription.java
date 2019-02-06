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
import javax.json.JsonString;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class ItemIdentifierDescription implements IJsonStreamable
{
	private String id;
	private String timestamp;
	private boolean deleted;
	private Collection<String> setspecs;

	public ItemIdentifierDescription(String id)
	{
		this.id = id;
		this.timestamp = "";
		this.deleted = false;
		this.setspecs = new ArrayList<>(1);
	}

	public String getId()
	{
		return id;
	}

	public String getTimestamp()
	{
		return timestamp;
	}

	public Instant getTimestampAsInstant()
	{
		return Instant.parse(timestamp);
	}

	public long getTimestampAsLong()
	{
		return this.getTimestampAsInstant().toEpochMilli();
	}

	public Date getTimestampAsDate()
	{
		return new Date(this.getTimestampAsLong());
	}

	public Collection<String> getSets()
	{
		return setspecs;
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public ItemIdentifierDescription setTimestamp(String timestamp)
	{
		this.timestamp = timestamp;
		return this;
	}

	public ItemIdentifierDescription setTimestamp(Instant timestamp)
	{
		return this.setTimestamp(timestamp.toString());
	}

	public ItemIdentifierDescription setTimestamp(Date timestamp)
	{
		return this.setTimestamp(timestamp.getTime());
	}

	public ItemIdentifierDescription setTimestamp(long timestamp)
	{
		return this.setTimestamp(Instant.ofEpochMilli(timestamp));
	}

	public ItemIdentifierDescription setDeleted(boolean deleted)
	{
		this.deleted = deleted;
		return this;
	}

	public ItemIdentifierDescription setSets(Collection<String> sets)
	{
		this.setspecs = sets;
		return this;
	}

	public void write(JsonGenerator json, String objname) throws JsonException
	{
		if (objname != null)
			json.writeStartObject(objname);
		else json.writeStartObject();

		json.write(Fields.ID, this.getId());
		json.write(Fields.TIMESTAMP, this.getTimestamp());
		json.write(Fields.DELETED, this.isDeleted());
		json.writeStartArray(Fields.SETSPECS);
		this.getSets().forEach(json::write);
		json.writeEnd();
		json.writeEnd();
	}

	public static ItemIdentifierDescription from(JsonObject json) throws JsonException
	{
		try {
			final List<String> sets = json.getJsonArray(Fields.SETSPECS)
					.getValuesAs((value) -> ((JsonString) value).getString());

			return new ItemIdentifierDescription(json.getString(Fields.ID))
					.setTimestamp(json.getString(Fields.TIMESTAMP))
					.setDeleted(json.getBoolean(Fields.DELETED))
					.setSets(sets);
		}
		catch (Exception error) {
			throw new JsonException("Constructing object failed!", error);
		}
	}

	public static ItemIdentifierDescription from(Response response) throws JsonException
	{
		final InputStream istream = response.readEntity(InputStream.class);
		try (final JsonParser parser = Json.createParser(istream)) {
			return ItemIdentifierDescription.from(parser.getObject());
		}
	}


	// ========== Internal Helpers ==============================

	private static final class Fields
	{
		static final String ID         = "id";
		static final String TIMESTAMP  = "timestamp";
		static final String DELETED    = "deleted";
		static final String SETSPECS   = "setspecs";
	}
}
