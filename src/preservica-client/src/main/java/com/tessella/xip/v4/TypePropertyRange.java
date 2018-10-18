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
 * A specific range of numeric values of a property (whose values are continuous) that have the same risk score.
 * 
 * <p>Java-Klasse für typePropertyRange complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typePropertyRange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MinValue" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="MaxValue" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typePropertyRange", propOrder = {
    "minValue",
    "maxValue"
})
public class TypePropertyRange {

    @XmlElement(name = "MinValue")
    protected double minValue;
    @XmlElement(name = "MaxValue")
    protected double maxValue;

    /**
     * Ruft den Wert der minValue-Eigenschaft ab.
     * 
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Legt den Wert der minValue-Eigenschaft fest.
     * 
     */
    public void setMinValue(double value) {
        this.minValue = value;
    }

    /**
     * Ruft den Wert der maxValue-Eigenschaft ab.
     * 
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Legt den Wert der maxValue-Eigenschaft fest.
     * 
     */
    public void setMaxValue(double value) {
        this.maxValue = value;
    }

}
