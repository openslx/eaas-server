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

package de.bwl.bwfla.metadata.repository.json;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;


public abstract class JsonReader implements Iterable<String>, AutoCloseable
{
	private final Runnable cleanup;
	protected final JsonParser json;

	protected JsonReader(InputStream istream) throws JsonParsingException
	{
		this(istream, null);
	}

	protected JsonReader(InputStream istream, Runnable cleanup) throws JsonParsingException
	{
		this.cleanup = cleanup;
		this.json = Json.createParser(new InputStreamReader(istream, StandardCharsets.UTF_8));

		JsonUtils.expect(json, JsonParser.Event.START_OBJECT);
	}

	public String advance() throws JsonParsingException
	{
		switch (json.next())
		{
			case KEY_NAME:
				return json.getString();
			case END_OBJECT:
				return null;
			default:
				throw JsonUtils.fail("Invalid parser state detected!", json);
		}
	}

	public void skipJsonObject()
	{
		json.skipObject();
	}

	public void skipJsonArray()
	{
		json.skipArray();
	}

	@Override
	public @NotNull Iterator<String> iterator()
	{
		return new BlockIterator(this);
	}

	@Override
	public void close()
	{
		json.close();
		if (cleanup != null)
			cleanup.run();
	}


	// ========== Internal Helpers ==============================

	private static class BlockIterator implements Iterator<String>
	{
		private final JsonReader reader;
		private String curname;

		private BlockIterator(JsonReader reader)
		{
			this.reader = reader;
			this.curname = null;
		}

		@Override
		public boolean hasNext()
		{
			curname = reader.advance();
			return (curname != null);
		}

		@Override
		public String next()
		{
			return curname;
		}
	}
}
