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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import static cz.jirutka.atom.jaxb.Namespaces.ATOM_NS;
import static cz.jirutka.atom.jaxb.Namespaces.OPENSEARCH_NS;

/**
 * Atom Feed
 *
 * <p>The "atom:feed" element is the document (i.e., top-level) element of
 * an Atom Feed Document, acting as a container for metadata and data
 * associated with the feed.  Its element children consist of metadata
 * elements followed by zero or more atom:entry child elements.</p>
 * 
 * <p><b>This class does not contain all elements defined in RF 4287, but only
 * some subset that is relevant for our purpose!</b></p>
 * 
 * <p>Atom Feed may contain entries of various types in general. However, in
 * many cases you have Feeds with entries of just one content's type and you 
 * do not want to bother yourself with explicit casting. Therefore this class
 * has generic typed {@linkplain entries}. If you want to create Feed that may
 * contain various types of entries without common superclass, just don't set 
 * its generic type or use type <tt>Object</tt>.</p>
 *
 * @param <T> type of entries {@link #content}
 *
 * @see <a href="http://tools.ietf.org/html/rfc4287#page-11">RFC 4287, Page 11</a>
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@XmlRootElement(namespace = ATOM_NS)
@XmlType(name = "atomFeed",
         propOrder = {"entries", "startIndex", "itemsPerPage", "totalResults"})
public class Feed <T> extends AtomSource {

    /**
     * The "atom:entry" element represents an individual entry, acting as a
     * container for metadata and data associated with the entry.
     *
     * <p>This class can contain entries of various content types. Use
     * {@link #getEntries(java.lang.Class)} to get only entries of some type in
     * type-safe manner.</p>
     *
     * @see http://tools.ietf.org/html/rfc4287#section-4.1.2
     */
    @XmlElementRef
    private List<Entry<T>> entries = new ArrayList<Entry<T>>();

    /**
     * The index of the first search result in the current set of search results.
     *
     * @see http://www.opensearch.org/Specifications/OpenSearch/1.1#Elements
     */
    @XmlElement(namespace = OPENSEARCH_NS)
    private Integer startIndex;

    /**
     * The number of search results returned per page.
     *
     * @see http://www.opensearch.org/Specifications/OpenSearch/1.1#Elements
     */
    @XmlElement(namespace = OPENSEARCH_NS)
    private Integer itemsPerPage;

    /**
     * The number of search results available for the current search.
     *
     * @see http://www.opensearch.org/Specifications/OpenSearch/1.1#Elements
     */
    @XmlElement(namespace = OPENSEARCH_NS)
    private Long totalResults;

    

    public Feed() {}
    /** 
     * @param entries {@link #entries}
     */
    public Feed(List<Entry<T>> entries) {
        this.entries = entries;
    }


    /** 
     * @param entry {@link #entries}
     */
    public void addEntry(Entry<T> entry) {
        this.entries.add(entry); 
    }

    /**
     * Returns {@linkplain Entry#content contents} from all {@linkplain entries}.
     *
     * @return contents of entries
     */
    public List<T> getContents() {
        List<T> contents = new ArrayList<T>(entries.size());
        for (Entry<T> entry : entries) {
            contents.add(entry.getContent());
        }
        return contents;
    }


    ////////  Accessors  ////////

    /** @return {@link #entries} */
    public List<Entry<T>> getEntries() { return entries; }

    /** @param entries {@link #entries} */
    public void setEntries(List<Entry<T>> entries) { this.entries = entries; }

    /** @return {@link #itemsPerPage} */
    public Integer getItemsPerPage() { return itemsPerPage; }

    /** @param itemsPerPage {@link #itemsPerPage} */
    public void setItemsPerPage(Integer itemsPerPage) { this.itemsPerPage = itemsPerPage; }

    /** @return {@link #startIndex} */
    public Integer getStartIndex() { return startIndex; }

    /** @param startIndex {@link #startIndex} */
    public void setStartIndex(Integer startIndex) { this.startIndex = startIndex; }

    /** @return {@link #totalResults} */
    public Long getTotalResults() { return totalResults; }

    /** @param totalResults {@link #totalResults} */
    public void setTotalResults(Long totalResults) { this.totalResults = totalResults; }
    
}
