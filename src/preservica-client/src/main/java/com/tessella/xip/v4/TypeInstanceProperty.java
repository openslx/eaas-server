//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.16 um 03:18:14 PM CET 
//


package com.tessella.xip.v4;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Details of an instance property of a file format from the Technical Registry.  An instance property is a property that varies between files of the same format, e.g. image height or number of pages.  Note that inherent properties are ones that all files of a given format share, such as the level of public disclosure for the format.
 * 
 * <p>Java-Klasse für typeInstanceProperty complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeInstanceProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PropertyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PropertyDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="PropertyValue" type="{http://www.tessella.com/XIP/v4}typePropertyValue"/>
 *           &lt;element name="PropertyRange" type="{http://www.tessella.com/XIP/v4}typePropertyRange"/>
 *         &lt;/choice>
 *         &lt;element name="PropertyRisk" type="{http://www.tessella.com/XIP/v4}typePropertyRisk"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="highRisk" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeInstanceProperty", propOrder = {
    "propertyName",
    "propertyDescription",
    "propertyValue",
    "propertyRange",
    "propertyRisk"
})
public class TypeInstanceProperty {

    @XmlElement(name = "PropertyName")
    protected String propertyName;
    @XmlElement(name = "PropertyDescription")
    protected String propertyDescription;
    @XmlElement(name = "PropertyValue")
    protected String propertyValue;
    @XmlElement(name = "PropertyRange")
    protected TypePropertyRange propertyRange;
    @XmlElement(name = "PropertyRisk")
    protected double propertyRisk;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "highRisk")
    protected Boolean highRisk;

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
     * Ruft den Wert der propertyDescription-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyDescription() {
        return propertyDescription;
    }

    /**
     * Legt den Wert der propertyDescription-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyDescription(String value) {
        this.propertyDescription = value;
    }

    /**
     * Ruft den Wert der propertyValue-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * Legt den Wert der propertyValue-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyValue(String value) {
        this.propertyValue = value;
    }

    /**
     * Ruft den Wert der propertyRange-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypePropertyRange }
     *     
     */
    public TypePropertyRange getPropertyRange() {
        return propertyRange;
    }

    /**
     * Legt den Wert der propertyRange-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypePropertyRange }
     *     
     */
    public void setPropertyRange(TypePropertyRange value) {
        this.propertyRange = value;
    }

    /**
     * Ruft den Wert der propertyRisk-Eigenschaft ab.
     * 
     */
    public double getPropertyRisk() {
        return propertyRisk;
    }

    /**
     * Legt den Wert der propertyRisk-Eigenschaft fest.
     * 
     */
    public void setPropertyRisk(double value) {
        this.propertyRisk = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der highRisk-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHighRisk() {
        return highRisk;
    }

    /**
     * Legt den Wert der highRisk-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHighRisk(Boolean value) {
        this.highRisk = value;
    }

}
