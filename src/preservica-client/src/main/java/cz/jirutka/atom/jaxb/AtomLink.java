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

import java.net.URI;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Atom Link
 *
 * <p>The "atom:link" element defines a reference from an entry or feed to
 * a Web resource.  This specification assigns no meaning to the content
 * (if any) of this element.</p>
 *
 * <p><b>This class does not contain all elements defined in RF 4287, but only
 * some subset that is relevant for our purpose!</b></p>
 *
 * @see <a href="http://tools.ietf.org/html/rfc4287#section-4.2.7">RFC 4287, Page 21</a>
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@XmlRootElement(name = "link")
@XmlType(name = "atomLink",
         propOrder = {"rel", "href", "type"})
public class AtomLink extends CommonAttributes {

    public static final String
            ALTERNATE = "alternate",
            NEXT = "next",
            PREV = "prev",
            RELATED = "related",
            SELF = "self";

    /**
     * The "href" attribute contains the link's IRI. atom:link elements MUST have
     * an href attribute, whose value MUST be a IRI reference [RFC3987].
     *
     * @see http://tools.ietf.org/html/rfc4287#section-4.2.7.1
     */
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "atomUri")
    private URI href;

    /**
     * The atom:link elements MAY have a "rel" attribute that indicates the
     * link relation type.  If the "rel" attribute is not present, the link
     * element MUST be interpreted as if the link relation type is "alternate".
     *
     * @see http://tools.ietf.org/html/rfc4287#section-4.2.7.2
     */
    @XmlAttribute
    @XmlSchemaType(name = "atomNCName")
    private String rel;

   /**
     * On the link element, the "type" attribute's value is an advisory media
     * type: it is a hint about the type of the representation that is expected
     * to be returned when the value of the href attribute is dereferenced...
     *
     * @see http://tools.ietf.org/html/rfc4287#section-4.2.7.3
     */
    @XmlAttribute
    @XmlSchemaType(name = "atomMediaType")
    private String type;



    public AtomLink() {}

    /**
     * @param rel {@link #rel}
     * @param href {@link #href}
     */
    public AtomLink(String rel, URI href) {
        this(rel, href, null);
    }

    /**
     * @param rel {@link #rel}
     * @param href {@link #href}
     */
    public AtomLink(String rel, String href) {
        this(rel, URI.create(href), null);
    }

    /**
     * @param rel {@link #rel}
     * @param href {@link #href}
     * @param type {@link #type}
     */
    public AtomLink(String rel, URI href, String type) {
        this.rel = rel;
        this.href = href;
        this.type = type;
    }


    ////////  Accessors  ////////

    /** @return {@link #href}  */
    public URI getHref() { return href; }

    /** @param href {@link #href} */
    public void setHref(URI href) { this.href = href; }

    /** @return {@link #rel}  */
    public String getRel() { return rel; }

    /** @param rel {@link #rel} */
    public void setRel(String rel) { this.rel = rel; }

    /** @return {@link #type}  */
    public String getType() { return type; }

    /** @param type {@link #type} */
    public void setType(String type) { this.type = type; }

}
