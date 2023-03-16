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

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.openslx.eaas.common.databind.DataUtils;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public static <T extends JaxbType> T fromValue(String value, Class<T> klass) throws JAXBException
    {
        return DataUtils.xml()
                .read(value, klass);
    }

    public static <T extends JaxbType> T from(InputStream source, Class<T> clazz) throws Exception
    {
        // NOTE: since input stream is always closed() on errors,
        //       we have to buffer data as array for reuse!
        final var bytes = source.readAllBytes();
        source = new ByteArrayInputStream(bytes);
        source.mark(1024 * 1024);
        try {
            // try to deserialize from XML with JAXB first!
            return JaxbType.fromXml(source, clazz);
        }
        catch (Exception error) {
            source.reset();  // rewind and retry!
            return JaxbType.fromJson(source, clazz);
        }
    }

    public static <T extends JaxbType> T fromXml(InputStream source, Class<T> clazz) throws JAXBException
    {
        return DataUtils.xml()
                .read(source, clazz);
    }

    public static <T extends JaxbType> T fromJson(InputStream source, Class<T> clazz) throws Exception
    {
        return DataUtils.json()
                .read(source, clazz);
    }

    /**
     * The usage of this method is limited, must be deleted as obsolete
     * @param value
     * @param klass
     * @param <T>
     * @return
     * @throws JAXBException
     */
    @Deprecated
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


    //Jaxb object from Json without root element
    private static <T extends JaxbType> T fromValueJackson(final String value,
                                                           final Class<T> klass,
                                                           ObjectMapper objectMapper) throws BWFLAException {
        JaxbType result = null;
        try {
            result = objectMapper.readValue(value, klass);
        } catch (IOException e) {
            throw new BWFLAException(e);
        }

        if(result == null)
            throw new BWFLAException("jackson failed to parse json!");


        return klass.cast(result);
    }

    //Jaxb object from Json without root element using Jackson as backend
    public static <T extends JaxbType> T fromJsonValueWithoutRoot(final String value,
                                                                  final Class<T> klass) throws BWFLAException {
        return fromValueJackson(value, klass, DataUtils.json().mapper());
    }

    //Jaxb object from Yaml using Jackson as backend
    public static <T extends JaxbType> T fromYamlValue(final String value,
                                                       final Class<T> klass) throws BWFLAException {
        return fromValueJackson(value, klass, new ObjectMapper(new YAMLFactory()));
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
        return DataUtils.xml()
                .write(this);
    }


    /**
     *  The usage of this method is limited, must be deleted as obsolete
     * @param prettyPrint
     * @return
     * @throws JAXBException
     */
    @Deprecated
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

    public String jsonValueWithoutRoot(final boolean prettyPrint) {
        return jacksonValue(prettyPrint, DataUtils.json().mapper());
    }
    public String yamlValue(final boolean prettyPrint) {
        return jacksonValue(prettyPrint, new ObjectMapper(new YAMLFactory()));
    }


    /**
     * Json value of Jaxb object without root element
     * @param prettyPrint
     * @return
     * @throws JAXBException
     */
    private String jacksonValue(final boolean prettyPrint, ObjectMapper objectMapper) {
        StringWriter writer = new StringWriter();
        try {
            objectMapper.writeValue(writer, this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    @Override
    public String toString() {
        try {
            return this.value(true);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
