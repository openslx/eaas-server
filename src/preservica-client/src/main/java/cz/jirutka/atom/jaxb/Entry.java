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

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import static cz.jirutka.atom.jaxb.Namespaces.ATOM_NS;

/**
 * Atom Entry
 *
 * <p>The "atom:entry" element represents an individual entry, acting as a
 * container for metadata and data associated with the entry.  This
 * element can appear as a child of the atom:feed element, or it can
 * appear as the document (i.e., top-level) element of a stand-alone
 * Atom Entry Document.</p>
 *
 * <p><b>This class does not contain all elements defined in RF 4287, but only
 * some subset that is relevant for our purpose!</b></p>
 *
 * @param <T> type of {@link #content} which this entry contains
 *
 * @see <a href="http://tools.ietf.org/html/rfc4287#page-13">RFC 4287, Page 13</a>
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@XmlRootElement(namespace = ATOM_NS)
@XmlType(name = "atomEntry",
         propOrder = {"published", "summary", "content"})
public class Entry <T> extends AtomSource {
    
    /**
     * The "atom:content" element either contains or links to the content of 
     * the entry. In this case, it's actually some entity in XML format.
     * 
     * @see http://tools.ietf.org/html/rfc4287#section-4.1.3
     */
    @XmlElement(namespace = ATOM_NS)
    @XmlSchemaType(name = "atomInlineOtherContent")
    private T content;

    /**
     * The "atom:published" element is a Date construct indicating an instant
     * in time associated with an event early in the life cycle of the entry.
     * Typically, atom:published will be associated with the initial creation
     * or first availability of the resource.
     *
     * @see http://tools.ietf.org/html/rfc4287#section-4.2.9
     */
    @XmlElement(namespace = ATOM_NS)
    @XmlSchemaType(name = "atomPublished")
    private Date published;

    /**
     * The "atom:summary" element is a Text construct that conveys a short
     * summary, abstract, or excerpt of an entry.
     *
     * @see http://tools.ietf.org/html/rfc4287#section-4.2.13
     */
    @XmlElement(namespace = ATOM_NS)
    @XmlSchemaType(name = "atomSummary")
    private String summary;


    
    public Entry() {}
    /** 
     * @param content {@link #content}
     */
    public Entry(T content) {
        this.content = content;
    }


    ////////  Accessors  ////////
    
    /** @return {@link #content} */
    public T getContent() { return content; }

    /** @param content {@link #content} */
    public void setContent(T content) { this.content = content; }

    /** @return {@link #published} */
    public Date getPublished() { return published; }

    /** @param published {@link #published} */
    public void setPublished(Date published) { this.published = published; }
   
    /** @return {@link #summary} */
    public String getSummary() { return summary; }

    /** @param summary {@link #summary} */
    public void setSummary(String summary) { this.summary = summary; }

}
