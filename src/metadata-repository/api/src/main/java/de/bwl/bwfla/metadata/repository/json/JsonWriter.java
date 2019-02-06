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
import javax.json.JsonException;
import javax.json.stream.JsonGenerator;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.stream.Stream;


public abstract class JsonWriter implements AutoCloseable
{
	private final JsonGenerator json;


	protected JsonWriter(OutputStream ostream) throws JsonException
	{
		this.json = Json.createGenerator(ostream);

		json.writeStartObject();
	}

	@Override
	public void close()
	{
		json.writeEnd();
		json.close();
	}


	// ========== Internal Helpers ==============================

	protected void write(String name, IJsonStreamable value) throws JsonException
	{
		value.write(json, name);
	}

	protected <T extends IJsonStreamable> void write(String name, Stream<T> values, int maxBufferedCount) throws JsonException
	{
		json.writeStartArray(name);
		values.forEach(new ValueWriter<>(json, maxBufferedCount));
		json.writeEnd();
	}

	protected void flush()
	{
		json.flush();
	}

	private static class ValueWriter<T extends IJsonStreamable> implements Consumer<T>
	{
		private final JsonGenerator json;
		private final int maxcount;
		private int counter;

		public ValueWriter(JsonGenerator json, int maxBufferedCount)
		{
			this.json = json;
			this.maxcount = maxBufferedCount;
			this.counter = 0;
		}

		@Override
		public void accept(T value)
		{
			value.write(json, null);
			if (++counter < maxcount)
				return;

			counter = 0;
			json.flush();
		}
	}
}
