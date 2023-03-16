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
 * PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.common.utils.jaxb;

import com.openslx.eaas.common.databind.DataUtils;
import de.bwl.bwfla.common.utils.PipedDataSource;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class JaxbCollectionWriter<T> implements Runnable
{
	private final Logger log;
	private final Stream<T> source;
	private final Class<T> klass;
	private final PipedDataSource pipe;

	public JaxbCollectionWriter(Stream<T> source, Class<T> klass, String name, String mimetype, Logger log) throws IOException
	{
		this.log = log;
		this.source = source;
		this.klass = klass;
		this.pipe = new PipedDataSource(name, mimetype);
	}

	public DataHandler getDataHandler()
	{
		return new DataHandler(pipe);
	}

	@Override
	public void run()
	{
		try (final OutputStream ostream = pipe.getOutputStream()) {
			final var writer = XMLOutputFactory.newFactory()
					.createXMLStreamWriter(ostream);

			try {
				writer.writeStartDocument();
				writer.writeStartElement(pipe.getName());

				source.forEach(new ItemMarshaller<>(writer, klass, log));

				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
			}
			finally {
				this.close(writer);
			}
		}
		catch (Throwable error) {
			log.log(Level.WARNING, "Writing '" + pipe.getName() + "' failed!", error);
		}
		finally {
			this.close(source);
		}
	}

	public void close()
	{
		this.close(source);
	}

	private void close(XMLStreamWriter writer)
	{
		if (writer == null)
			return;

		try {
			writer.close();
		}
		catch (Throwable error) {
			log.log(Level.WARNING, "Closing xml-writer for '" + pipe.getName() + "' failed!", error);
		}
	}

	private void close(AutoCloseable resource)
	{
		if (resource == null)
			return;

		try {
			resource.close();
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Closing resource for '" + pipe.getName() + "' failed!", error);
		}
	}

	private static class ItemMarshaller<I> implements Consumer<I>
	{
		private final XMLStreamWriter writer;
		private final Marshaller marshaller;
		private final Logger log;

		public ItemMarshaller(XMLStreamWriter writer, Class<I> klass, Logger log) throws JAXBException
		{
			this.writer = writer;
			this.log = log;

			this.marshaller = DataUtils.xml()
					.marshaller(klass, false);

			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		}

		@Override
		public void accept(I item)
		{
			try {
				marshaller.marshal(item, writer);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Marshalling item failed!", error);
				throw new RuntimeException(error);
			}
		}
	}
}
