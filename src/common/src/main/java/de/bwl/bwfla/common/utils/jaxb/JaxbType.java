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

package de.bwl.bwfla.common.utils.jaxb;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.stream.StreamSource;

@XmlTransient
public abstract class JaxbType {
    public static <T extends JaxbType> T fromValue(final String value,
            final Class<T> klass) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(klass);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        T result = klass.cast(unmarshaller
                .unmarshal(new StreamSource(new StringReader(value))));
        JaxbValidator.validate(result);
        return result;
    }
    public static <T extends JaxbType> T fromJsonValue(final String value,
            final Class<T> klass) throws JAXBException {

        //Set the various properties you want
        Map<String, Object> properties = new HashMap<>();
        properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
        properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, true);

        //Create a Context using the properties
        JAXBContext jaxbContext =
                JAXBContextFactory.createContext(new Class[]  {
                        klass}, properties);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        //Unmarshall the object
        T result = klass.cast(unmarshaller.unmarshal(new StreamSource(new StringReader(value))));

        JaxbValidator.validate(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T extends JaxbType> T fromValue(final String value,
            final T object) throws JAXBException {
        return JaxbType.fromValue(value, (Class<T>) object.getClass());
    }

    /**
     * Returns a compact string representation, in XML text format, of this
     * instance.
     * 
     * @return A string representation of this object.
     * @throws JAXBException if the object cannot be successfully conerted to a
     *             string representation
     */
    public String value() throws JAXBException {
        return this.value(false);
    }

    /**
     * Returns a compact string representation, in XML text format, of this
     * instance.
     * 
     * @param prettyPrint if true, the XML text output will be indented with
     *            whitespace according to the nesting level of XML elements
     * @return A string representation of this object.
     * @throws JAXBException if the object cannot be successfully conerted to a
     *             string representation
     */
    public String value(final boolean prettyPrint) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(this.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint);
        StringWriter w = new StringWriter();
        marshaller.marshal(this, w);
        return w.toString();
    }


    /**
     * Reworked JSONvalue, which returns Jaxb object as JSON String
     * @param prettyPrint
     * @return
     * @throws JAXBException
     */
    public String JSONvalue(final boolean prettyPrint) throws JAXBException {

        Map<String, Object> properties = new HashMap<>();
        properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
        properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, true);
        properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint);

        //Create a Context using the properties
        JAXBContext jc =
                JAXBContextFactory.createContext(new Class[]  {this.getClass()}, properties);

        Marshaller marshaller = jc.createMarshaller();

        StringWriter w = new StringWriter();
        marshaller.marshal(this, w);
        return w.toString();
    }

    @Override
    public String toString() {
        try {
            return this.value(true);
        } catch (JAXBException e) {
            return "Error converting JAXB type to string: " + e.getMessage();
        }
    }
}
