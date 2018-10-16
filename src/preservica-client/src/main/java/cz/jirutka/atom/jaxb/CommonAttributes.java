/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.jirutka.atom.jaxb;

import java.io.Serializable;
import java.net.URI;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import static cz.jirutka.atom.jaxb.Namespaces.XML_NS;

/**
 * Attributes common across all atom types.
 *
 * @see <a href="http://tools.ietf.org/html/rfc4287#page-6">RFC 4287, Page 6</a>
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@XmlType(name = "atomCommonAttributes")
public abstract class CommonAttributes implements Serializable {

    /**
     * When xml:base is used in an Atom Document, it serves the function
     * described in <a href="http://tools.ietf.org/html/rfc3986#section-5.1.1">
     * section 5.1.1 of [RFC3986]</a>, establishing the base URI (or IRI) for
     * resolving any relative references found within the effective scope of
     * the xml:base attribute.
     *
     * @see http://tools.ietf.org/html/rfc4287#page-6
     */
    @XmlAttribute(namespace = XML_NS)
    @XmlSchemaType(name = "atomUri")
    private URI base;

    /**
     * Any element defined by this specification MAY have an xml:lang attribute,
     * whose content indicates the natural language for the element and its
     * descendents. The language context is only significant for elements and
     * attributes declared to be "Language-Sensitive" by this specification.
     *
     * @see http://tools.ietf.org/html/rfc4287#page-6
     */
    @XmlAttribute(namespace = XML_NS)
    @XmlSchemaType(name = "atomLanguageTag")
    private String lang;

    
    
    ////////  Accessors  ////////

    /** @return {@link #base} */
    public URI getBase() { return base; }

    /** @param base {@link #base} */
    public void setBase(URI base) { this.base = base; }
    
    /** @return {@link #lang} */
    public String getLang() { return lang; }

    /** @param lang {@link #lang} */
    public void setLang(String lang) { this.lang = lang; }
}
