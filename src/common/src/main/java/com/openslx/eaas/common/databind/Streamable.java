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

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
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
	private final List<Runnable> cleanups;
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

		Streamable.cleanup(cleanups);
	}

	public static <T> Streamable<T> of(Stream<T> stream)
	{
		return new Streamable<>(stream, Collections.emptyList());
	}

	@JsonCreator
	public static <T> Streamable<T> of(Collection<T> collection)
	{
		// NOTE: it is currently not possible to lazily deserialize data as a Stream<T>,
		//       so let Jackson eagerly parse it as a collection instead and wrap that!

		return new Streamable<>(collection, Collections.emptyList());
	}

	public static <T> Streamable<T> of(InputStream input, Class<T> clazz) throws BWFLAException
	{
		return Streamable.of(input, clazz, new ArrayList<>(2));
	}

	public static <T> Streamable<T> of(Response response, Class<T> clazz) throws BWFLAException
	{
		final var cleanups = new ArrayList<Runnable>(4);
		cleanups.add(() -> Streamable.close(response, "response"));
		try {
			final var input = response.readEntity(InputStream.class);
			return Streamable.of(input, clazz, cleanups);
		}
		catch (BWFLAException error) {
			Streamable.cleanup(cleanups);
			throw error;
		}
	}


	// ===== Internal Helpers ==============================

	private Streamable(Stream<T> stream, List<Runnable> cleanups)
	{
		if (stream == null)
			throw new IllegalArgumentException();

		this.cleanups = cleanups;
		this.stream = stream;
		this.collection = null;
	}

	private Streamable(Collection<T> collection, List<Runnable> cleanups)
	{
		if (collection == null)
			throw new IllegalArgumentException();

		this.cleanups = cleanups;
		this.collection = collection;
		this.stream = null;
	}

	private static <T> Streamable<T> of(InputStream input, Class<T> clazz, List<Runnable> cleanups)
			throws BWFLAException
	{
		cleanups.add(() -> Streamable.close(input, "input-stream"));
		try {
			final MappingIterator<T> iterator = DataUtils.json()
					.reader()
					.forType(clazz)
					.readValues(input);

			cleanups.add(() -> Streamable.close(iterator, "input-parser"));

			final var spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
			final var stream = StreamSupport.stream(spliterator, false);
			return new Streamable<>(stream, cleanups);
		}
		catch (Exception error) {
			Streamable.cleanup(cleanups);
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

	private static void cleanup(List<Runnable> tasks)
	{
		// run given tasks in reverse order!
		for (int i = tasks.size() - 1; i >= 0; --i)
			tasks.get(i).run();
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
