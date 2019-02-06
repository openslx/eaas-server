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
import de.bwl.bwfla.metadata.repository.json.JsonReader;
import de.bwl.bwfla.metadata.repository.json.JsonUtils;
import de.bwl.bwfla.metadata.repository.json.JsonWriter;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;


/** A JSON-stream of {@link ItemDescription}s, prefixed by a header. */
public class ItemDescriptionStream extends JsonReader
{
	public static ItemDescriptionStream create(Response response)
	{
		final InputStream istream = response.readEntity(InputStream.class);
		return new ItemDescriptionStream(istream, response::close);
	}

	public static ItemDescriptionStream create(InputStream istream)
	{
		return new ItemDescriptionStream(istream);
	}

	public Header getHeader()
	{
		JsonUtils.expect(json, JsonParser.Event.START_OBJECT);
		return Header.from(json.getObject());
	}

	public Stream<ItemDescription> getItemStream()
	{
		JsonUtils.expect(json, JsonParser.Event.START_ARRAY);
		return json.getArrayStream()
				.map((value) -> ItemDescription.from(value.asJsonObject()));
	}

	/** Supported parts/sections of the stream. */
	public static class Parts
	{
		public static final String HEADER = "header";
		public static final String ITEMS  = "items";
	}

	/** Stream's header */
	public static class Header implements IJsonStreamable
	{
		private int totalCount;

		public Header()
		{
			this.totalCount = -1;
		}

		public int getTotalCount()
		{
			return totalCount;
		}

		public Header setTotalCount(int count)
		{
			this.totalCount = count;
			return this;
		}

		@Override
		public void write(JsonGenerator json, String objname) throws JsonException
		{
			if (objname != null)
				json.writeStartObject(objname);
			else json.writeStartObject();

			json.write(Fields.TOTAL_COUNT, this.getTotalCount());
			json.writeEnd();
		}

		public static Header from(JsonObject json) throws JsonException
		{
			try {
				return new Header()
						.setTotalCount(json.getInt(Fields.TOTAL_COUNT));
			}
			catch (Exception error) {
				throw new JsonException("Constructing object failed!", error);
			}
		}
	}

	/** Specialized writer for streaming {@link ItemDescription}s. */
	public static class Writer extends JsonWriter
	{
		public Writer(OutputStream ostream) throws JsonException
		{
			super(ostream);
		}

		public Writer write(Header header) throws JsonException
		{
			super.write(Parts.HEADER, header);
			super.flush();
			return this;
		}

		public Writer write(Stream<ItemDescription> items) throws JsonException
		{
			super.write(Parts.ITEMS, items, 4);
			return this;
		}
	}


	// ========== Internal Helpers ==============================

	private static final class Fields
	{
		static final String TOTAL_COUNT = "total_count";
	}

	private ItemDescriptionStream(InputStream istream)
	{
		super(istream);
	}

	private ItemDescriptionStream(InputStream istream, Runnable cleanup)
	{
		super(istream, cleanup);
	}
}
