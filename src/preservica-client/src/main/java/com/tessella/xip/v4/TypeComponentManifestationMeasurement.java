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
 * The value of a Component Property as measured in a particular Component Manifestation.
 * 
 * <p>Java-Klasse für typeComponentManifestationMeasurement complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeComponentManifestationMeasurement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PropertyRef" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PropertyName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MeasurementEvent" type="{http://www.tessella.com/XIP/v4}typeEvent"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeComponentManifestationMeasurement", propOrder = {
    "propertyRef",
    "propertyName",
    "value",
    "measurementEvent"
})
public class TypeComponentManifestationMeasurement {

    @XmlElement(name = "PropertyRef", required = true)
    protected String propertyRef;
    @XmlElement(name = "PropertyName", required = true)
    protected String propertyName;
    @XmlElement(name = "Value", required = true)
    protected String value;
    @XmlElement(name = "MeasurementEvent", required = true)
    protected TypeEvent measurementEvent;

    /**
     * Ruft den Wert der propertyRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyRef() {
        return propertyRef;
    }

    /**
     * Legt den Wert der propertyRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyRef(String value) {
        this.propertyRef = value;
    }

    /**
     * Ruft den Wert der propertyName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Legt den Wert der propertyName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyName(String value) {
        this.propertyName = value;
    }

    /**
     * Ruft den Wert der value-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Ruft den Wert der measurementEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getMeasurementEvent() {
        return measurementEvent;
    }

    /**
     * Legt den Wert der measurementEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setMeasurementEvent(TypeEvent value) {
        this.measurementEvent = value;
    }

}
