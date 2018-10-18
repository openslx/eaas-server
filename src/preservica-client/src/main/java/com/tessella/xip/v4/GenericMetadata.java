//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.16 um 03:18:14 PM CET 
//


package com.tessella.xip.v4;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * Arbitrary contents, which may conform to an XML schema.  Used to store extra metadata, particularly descriptive metadata (i.e. cataloguing information), from another schema (e.g. Dublin Core, METS, MODS, ISAD(G), etc.) that is relevant to a particular entity.  Allows for controlled extension of the XIP schema.
 * 
 * <p>Java-Klasse für genericMetadata complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="genericMetadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any processContents='skip' minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="schemaURI" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "genericMetadata", propOrder = {
    "any"
})
public class GenericMetadata {

    @XmlAnyElement
    protected Element any;
    @XmlAttribute(name = "schemaURI")
    protected String schemaURI;

    /**
     * Ruft den Wert der any-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Element }
     *     
     */
    public Element getAny() {
        return any;
    }

    /**
     * Legt den Wert der any-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Element }
     *     
     */
    public void setAny(Element value) {
        this.any = value;
    }

    /**
     * Ruft den Wert der schemaURI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemaURI() {
        return schemaURI;
    }

    /**
     * Legt den Wert der schemaURI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemaURI(String value) {
        this.schemaURI = value;
    }

}
