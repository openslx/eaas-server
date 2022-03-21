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

package com.openslx.eaas.common.databind;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.TaskStack;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Simple wrapper for {@link java.util.stream.Stream}
 * objects to make them de/serializable in JAX-RS APIs
 */
@JsonSerialize(using = Streamable.Serializer.class)
public class Streamable<T> implements AutoCloseable
{
	private final TaskStack cleanups;
	private final Stream<T> stream;
	private Collection<T> collection;


	public Collection<T> collection()
	{
		// there must always be a valid stream or collection!
		if (collection == null)
			collection = stream.collect(Collectors.toList());

		return collection;
	}

	public Stream<T> stream()
	{
		if (stream != null)
			return stream;

		return this.collection()
				.stream();
	}

	public Iterator<T> iterator()
	{
		if (stream != null)
			return stream.iterator();

		return this.collection()
				.iterator();
	}

	@Override
	public void close()
	{
		if (stream != null)
			stream.close();

		if (cleanups != null)
			cleanups.execute();
	}

	public static <T> Streamable<T> of(Stream<T> stream)
	{
		return new Streamable<>(stream, null);
	}

	public static <T> Streamable<T> of(Stream<T> stream, TaskStack cleanups)
	{
		return new Streamable<>(stream, cleanups);
	}

	public static <T,U> Streamable<U> of(Stream<T> stream, Function<T,U> mapper)
	{
		return Streamable.of(stream, mapper, null);
	}

	public static <T,U> Streamable<U> of(Stream<T> stream, Function<T,U> mapper, TaskStack cleanups)
	{
		return new Streamable<>(stream.map(mapper), cleanups);
	}

	@JsonCreator
	public static <T> Streamable<T> of(Collection<T> collection)
	{
		// NOTE: it is currently not possible to lazily deserialize data as a Stream<T>,
		//       so let Jackson eagerly parse it as a collection instead and wrap that!

		return new Streamable<>(collection, null);
	}

	public static <T,U> Streamable<U> of(Collection<T> collection, Function<T,U> mapper)
	{
		final var stream = collection.stream()
				.map(mapper);

		return new Streamable<>(stream, null);
	}

	public static <T> Streamable<T> of(InputStream input, Class<T> clazz) throws BWFLAException
	{
		final Function<T,T> mapper = (value) -> value;
		return Streamable.of(input, clazz, mapper);
	}

	public static <T,U> Streamable<U> of(InputStream input, Class<T> clazz, Function<T,U> mapper) throws BWFLAException
	{
		return Streamable.of(input, clazz, mapper, new TaskStack(2));
	}

	public static <T> Streamable<T> of(Response response, Class<T> clazz) throws BWFLAException
	{
		final Function<T,T> mapper = (value) -> value;
		return Streamable.of(response, clazz, mapper);
	}

	public static <T,U> Streamable<U> of(Response response, Class<T> clazz, Function<T,U> mapper) throws BWFLAException
	{
		final var cleanups = new TaskStack(4);
		cleanups.push("close-response", () -> Streamable.close(response, "response"));
		try {
			final var input = response.readEntity(InputStream.class);
			return Streamable.of(input, clazz, mapper, cleanups);
		}
		catch (BWFLAException error) {
			cleanups.execute();
			throw error;
		}
	}


	// ===== Internal Helpers ==============================

	private Streamable(Stream<T> stream, TaskStack cleanups)
	{
		if (stream == null)
			throw new IllegalArgumentException();

		this.cleanups = cleanups;
		this.stream = stream;
		this.collection = null;
	}

	private Streamable(Collection<T> collection, TaskStack cleanups)
	{
		if (collection == null)
			throw new IllegalArgumentException();

		this.cleanups = cleanups;
		this.collection = collection;
		this.stream = null;
	}

	private static <T,U> Streamable<U> of(InputStream input, Class<T> clazz, Function<T,U> mapper, TaskStack cleanups)
			throws BWFLAException
	{
		cleanups.push("close-input-stream", () -> Streamable.close(input, "input-stream"));
		try {
			final MappingIterator<T> iterator = DataUtils.json()
					.reader()
					.forType(clazz)
					.readValues(input);

			cleanups.push("close-input-parser", () -> Streamable.close(iterator, "input-parser"));

			final var spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
			final var stream = StreamSupport.stream(spliterator, false)
					.map(mapper);

			return new Streamable<>(stream, cleanups);
		}
		catch (Exception error) {
			cleanups.execute();
			throw new BWFLAException("Preparing streamable failed!", error);
		}
	}

	private static void close(AutoCloseable closeable, String name)
	{
		try {
			closeable.close();
		}
		catch (Exception error) {
			final var logger = Logger.getLogger(Streamable.class.getSimpleName());
			logger.log(Level.WARNING, "Closing " + name + " failed!", error);
		}
	}


	/** Custom serializer for generic streamables */
	public static class Serializer extends JsonSerializer<Streamable<?>>
	{
		@Override
		public void serialize(Streamable<?> streamable, JsonGenerator generator, SerializerProvider provider)
				throws IOException
		{
			try (streamable) {
				// Streamable<T> is serialized as plain JSON array!
				generator.writeStartArray();
				for (final var iter = streamable.iterator(); iter.hasNext();)
					generator.writeObject(iter.next());

				generator.writeEndArray();
			}
		}
	}
}
