//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.16 um 03:18:14 PM CET 
//


package com.tessella.xip.v4;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Holds the checksum value, and the algorithm used to generate the checksum, for a Digital File.  More than one checksum value (i.e. more than one algorithm) may be recorded.
 * 
 * <p>Java-Klasse für typeFixityInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeFixityInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FixityAlgorithmRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="FixityValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeFixityInfo", propOrder = {
    "fixityAlgorithmRef",
    "fixityValue"
})
public class TypeFixityInfo {

    @XmlElement(name = "FixityAlgorithmRef")
    protected long fixityAlgorithmRef;
    @XmlElement(name = "FixityValue", required = true)
    protected String fixityValue;

    /**
     * Ruft den Wert der fixityAlgorithmRef-Eigenschaft ab.
     * 
     */
    public long getFixityAlgorithmRef() {
        return fixityAlgorithmRef;
    }

    /**
     * Legt den Wert der fixityAlgorithmRef-Eigenschaft fest.
     * 
     */
    public void setFixityAlgorithmRef(long value) {
        this.fixityAlgorithmRef = value;
    }

    /**
     * Ruft den Wert der fixityValue-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFixityValue() {
        return fixityValue;
    }

    /**
     * Legt den Wert der fixityValue-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFixityValue(String value) {
        this.fixityValue = value;
    }

}
