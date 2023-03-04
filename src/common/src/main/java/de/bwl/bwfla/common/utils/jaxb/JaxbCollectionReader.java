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

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class JaxbCollectionReader<T> implements Iterator<T>, AutoCloseable
{
	private final Logger log;
	private final String name;
	private final Class<T> klass;
	private final XMLStreamReader reader;
	private final Unmarshaller unmarshaller;

	public JaxbCollectionReader(Source source, Class<T> klass, String name, Logger log) throws XMLStreamException, JAXBException
	{
		this.log = log;
		this.name = name;
		this.klass = klass;
		this.unmarshaller = DataUtils.xml()
				.unmarshaller(klass);

		this.reader = XMLInputFactory.newFactory()
				.createXMLStreamReader(source);

		try {
			// Find collection start...
			if (reader.nextTag() != XMLStreamConstants.START_ELEMENT || !name.equals(reader.getName().toString())) {
				final String message = "Unexpected element: " + reader.getName() + " != " + name;
				throw new XMLStreamException(message, reader.getLocation());
			}

			// Find first item
			reader.nextTag();
		}
		catch (XMLStreamException error) {
			this.close();
			throw error;
		}
	}

	@Override
	public boolean hasNext()
	{
		try {
			return reader.hasNext() && !reader.isEndElement();
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Checking next item in '" + name + "' failed!", error);
			return false;
		}
	}

	@Override
	public T next()
	{
		try {
			return klass.cast(unmarshaller.unmarshal(reader));
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Reading item from '" + name + "' failed!", error);
			return null;
		}
	}

	@Override
	public void close()
	{
		this.close(reader);
	}

	public Stream<T> stream()
	{
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, 0), false)
				.onClose(this::close);
	}

	private void close(XMLStreamReader reader)
	{
		if (reader == null)
			return;

		try {
			reader.close();
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Closing xml-reader for '" + name + "' failed!", error);
		}
	}
}
