/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.common.databind;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;


public class XmlDataUtils
{
	public <T> T read(InputStream source, Class<T> clazz) throws JAXBException
	{
		final var unmarshaller = this.unmarshaller(clazz);
		final var value = unmarshaller.unmarshal(source);
		return clazz.cast(value);
	}

	public <T> T read(Reader source, Class<T> clazz) throws JAXBException
	{
		final var unmarshaller = this.unmarshaller(clazz);
		final var value = unmarshaller.unmarshal(source);
		return clazz.cast(value);
	}

	public <T> T read(StreamSource source, Class<T> clazz) throws JAXBException
	{
		final var unmarshaller = this.unmarshaller(clazz);
		final var value = unmarshaller.unmarshal(source);
		return clazz.cast(value);
	}

	public <T> T read(String source, Class<T> clazz) throws JAXBException
	{
		return this.read(new StringReader(source), clazz);
	}

	public <T> void write(OutputStream output, T value) throws JAXBException
	{
		final var marshaller = this.marshaller(value.getClass());
		marshaller.marshal(value, output);
	}

	public <T> void write(Writer writer, T value) throws JAXBException
	{
		final var marshaller = this.marshaller(value.getClass());
		marshaller.marshal(value, writer);
	}

	public <T> String write(T value) throws JAXBException
	{
		final var writer = new StringWriter();
		this.write(writer, value);
		return writer.toString();
	}

	public JAXBContext context(Class<?> clazz)
	{
		return this.lookup(clazz)
				.context();
	}

	public Marshaller marshaller(Class<?> clazz)
	{
		return this.marshaller(clazz, true);
	}

	public Marshaller marshaller(Class<?> clazz, boolean cached)
	{
		return this.lookup(clazz)
				.marshaller(cached);
	}

	public Unmarshaller unmarshaller(Class<?> clazz)
	{
		return this.unmarshaller(clazz, true);
	}

	public Unmarshaller unmarshaller(Class<?> clazz, boolean cached)
	{
		return this.lookup(clazz)
				.unmarshaller(cached);
	}


	// ===== Internal Helpers ==============================

	private final ConcurrentMap<String, CacheEntry> cache;

	XmlDataUtils()
	{
		this.cache = new ConcurrentHashMap<>();
	}

	private CacheEntry lookup(Class<?> clazz)
	{
		final Function<String, CacheEntry> constructor =
				(key) -> new CacheEntry(clazz);

		return cache.computeIfAbsent(clazz.getName(), constructor);
	}

	/**
	 * A wrapper for cached JAXB context and derived marshaller + unmarshaller for a given class.
	 * <p/>
	 * According to docs, contexts are thread-safe and can be shared between threads freely, but marshallers
	 * and unmarshallers are not required to be thread-safe and their behavior can vary between implementations.
	 * <p/>
	 * As a workaround, marshallers and unmarshallers are additionally cached in thread-local variables for
	 * later reuse by the same threads to partially reduce their recreation overhead.
	 */

	private static class CacheEntry
	{
		private final JAXBContext context;
		private final ThreadLocal<Marshaller> marshaller;
		private final ThreadLocal<Unmarshaller> unmarshaller;

		public CacheEntry(Class<?> clazz)
		{
			this.context = XmlDataUtils.createContext(clazz);
			this.marshaller = ThreadLocal.withInitial(() -> XmlDataUtils.createMarshaller(context));
			this.unmarshaller = ThreadLocal.withInitial(() -> XmlDataUtils.createUnmarshaller(context));
		}

		public JAXBContext context()
		{
			return context;
		}

		public Marshaller marshaller()
		{
			return this.marshaller(true);
		}

		public Marshaller marshaller(boolean cached)
		{
			return (cached) ? marshaller.get() : XmlDataUtils.createMarshaller(context);
		}

		public Unmarshaller unmarshaller()
		{
			return this.unmarshaller(true);
		}

		public Unmarshaller unmarshaller(boolean cached)
		{
			return (cached) ? unmarshaller.get() : XmlDataUtils.createUnmarshaller(context);
		}
	}

	private static JAXBContext createContext(Class<?> clazz)
	{
		try {
			return JAXBContext.newInstance(clazz);
		}
		catch (Exception error) {
			throw new IllegalStateException(error);
		}
	}

	private static Marshaller createMarshaller(JAXBContext context)
	{
		try {
			final var marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			return marshaller;
		}
		catch (Exception error) {
			throw new IllegalStateException(error);
		}
	}

	private static Unmarshaller createUnmarshaller(JAXBContext context)
	{
		try {
			return context.createUnmarshaller();
		}
		catch (Exception error) {
			throw new IllegalStateException(error);
		}
	}
}
