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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import static cz.jirutka.atom.jaxb.Namespaces.ATOM_NS;

/**
 * A Person construct is an element that describes a person, corporation, or
 * similar entity (hereafter, 'person').
 *
 * @see <a href="http://tools.ietf.org/html/rfc4287#section-3.2">RFC4287, Page 9</a>
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@XmlRootElement(name = "person")
@XmlType(name = "atomPersonConstruct")
public class AtomPerson extends CommonAttributes {

    /**
     * The "atom:name" element's content conveys a human-readable name for the
     * person.  The content of atom:name is Language-Sensitive. Person
     * constructs MUST contain exactly one "atom:name" element.
     *
     * @see http://tools.ietf.org/html/rfc4287#section-3.2.1
     */
    @XmlElement(namespace = ATOM_NS)
    @XmlSchemaType(name = "text")
    private String name;

    /**
     * The "atom:uri" element's content conveys an IRI associated with the
     * person.  Person constructs MAY contain an atom:uri element, but MUST NOT
     * contain more than one.  The content of atom:uri in a Person construct
     * MUST be an IRI reference [RFC3987].

     * @see http://tools.ietf.org/html/rfc4287#section-3.2.2
     */
    @XmlElement(namespace = ATOM_NS)
    @XmlSchemaType(name = "atomUri")
    private URI uri;

    /**
     * The "atom:email" element's content conveys an e-mail address associated
     * with the person.  Person constructs MAY contain an atom:email element,
     * but MUST NOT contain more than one.  Its content MUST conform to the
     * "addr-spec" production in [RFC2822].
     *
     * @see http://tools.ietf.org/html/rfc4287#section-3.2.3
     */
    @XmlElement(namespace = ATOM_NS)
    @XmlSchemaType(name = "atomEmailAddress")
    private String email;

    
    public AtomPerson() {}
    /** 
     * @param name {@link #name}
     */
    public AtomPerson(String name) {
        this.name = name;
    }


    ////////  Accessors  ////////

    /** @return {@link #name} */
    public String getName() { return name; }

    /** @param name {@link #name} */
    public void setName(String name) { this.name = name; }

    /** @return {@link #uri} */
    public URI getUri() { return uri; }

    /** @param uri {@link #uri} */
    public void setUri(URI uri) { this.uri = uri; }

    /** @return {@link #email} */
    public String getEmail() { return email; }

    /** @param email {@link #email} */
    public void setEmail(String email) { this.email = email; }
}
