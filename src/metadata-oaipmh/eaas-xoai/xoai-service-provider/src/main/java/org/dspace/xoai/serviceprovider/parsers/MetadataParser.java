/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.xoai.serviceprovider.parsers;

import com.lyncode.xml.XmlReader;
import com.lyncode.xml.exceptions.XmlReaderException;
import org.dspace.xoai.model.xoai.Element;
import org.dspace.xoai.model.xoai.Field;
import org.dspace.xoai.model.xoai.XOAIMetadata;
import org.hamcrest.Matcher;

import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;

import static com.lyncode.xml.matchers.QNameMatchers.localPart;
import static com.lyncode.xml.matchers.XmlEventMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;

public class MetadataParser {
    public XOAIMetadata parse(InputStream input) throws XmlReaderException {
        XOAIMetadata metadata = new XOAIMetadata();
        XmlReader reader = new XmlReader(input);
        reader.next(elementName(localPart(equalTo("metadata"))));

        while (reader.next(theEndOfDocument(), anEndElement(), startElement()).current(startElement())) {
            metadata.withElement(parseElement(reader));
        }

        return metadata;
    }

    // NOTE: EaaS specific patched version, that parses and returns the metadata as a string
    public String parseString(InputStream input) throws XmlReaderException {
        final StringBuilder metadata = new StringBuilder(2048);
        final XmlReader reader = new XmlReader(input);
        try {
            while (reader.next(theEndOfDocument(), aStartElement(), anEndElement(), endOfMetadata()).current(aStartElement()))
                metadata.append(reader.retrieveCurrentAsString());

            return metadata.toString();
        }
        finally {
            reader.close();
        }
    }

    private Element parseElement(XmlReader reader) throws XmlReaderException {
        Element element = new Element(reader.getAttributeValue(name()));
        while (reader.next(startElement(), startField(), endOfMetadata()).current(startElement())) {
            element.withElement(parseElement(reader));
        }

        while (reader.current(startField())) {
            Field field = new Field()
                    .withName(reader.getAttributeValue(name()));

            if (reader.next(anEndElement(), text()).current(text()))
                field.withValue(reader.getText());

            element.withField(field);
            reader.next(startField(), endElement());
        }

        return element;
    }

    private Matcher<XMLEvent> startField() {
        return allOf(aStartElement(), elementName(localPart(equalTo("field"))));
    }

    private Matcher<XMLEvent> endOfMetadata() {
        return allOf(anEndElement(), elementName(localPart(equalTo("metadata"))));
    }

    private Matcher<QName> name() {
        return localPart(equalTo("name"));
    }

    private Matcher<XMLEvent> startElement() {
        return allOf(aStartElement(), elementName(localPart(equalTo("element"))));
    }
    private Matcher<XMLEvent> endElement() {
        return allOf(anEndElement(), elementName(localPart(equalTo("element"))));
    }
}
