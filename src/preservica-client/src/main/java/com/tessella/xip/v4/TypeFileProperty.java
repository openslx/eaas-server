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
 * The value of a measured characteristic of a Digital File. N.B. These properties will help to ascertain whether migration is necessary and, if so, which migration pathway should be followed.
 * 
 * <p>Java-Klasse für typeFileProperty complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeFileProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PropertyRef" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FilePropertyName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Metadata" type="{http://www.tessella.com/XIP/v4}genericMetadata" minOccurs="0"/>
 *         &lt;element name="EventRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *       &lt;/sequence>
 *       &lt;attribute name="status" use="required" type="{http://www.tessella.com/XIP/v4}typeStatus" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeFileProperty", propOrder = {
    "propertyRef",
    "filePropertyName",
    "value",
    "metadata",
    "eventRef"
})
public class TypeFileProperty {

    @XmlElement(name = "PropertyRef", required = true)
    protected String propertyRef;
    @XmlElement(name = "FilePropertyName", required = true)
    protected String filePropertyName;
    @XmlElement(name = "Value", required = true)
    protected String value;
    @XmlElement(name = "Metadata")
    protected GenericMetadata metadata;
    @XmlElement(name = "EventRef")
    protected long eventRef;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

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
     * Ruft den Wert der filePropertyName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilePropertyName() {
        return filePropertyName;
    }

    /**
     * Legt den Wert der filePropertyName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilePropertyName(String value) {
        this.filePropertyName = value;
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
     * Ruft den Wert der metadata-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GenericMetadata }
     *     
     */
    public GenericMetadata getMetadata() {
        return metadata;
    }

    /**
     * Legt den Wert der metadata-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericMetadata }
     *     
     */
    public void setMetadata(GenericMetadata value) {
        this.metadata = value;
    }

    /**
     * Ruft den Wert der eventRef-Eigenschaft ab.
     * 
     */
    public long getEventRef() {
        return eventRef;
    }

    /**
     * Legt den Wert der eventRef-Eigenschaft fest.
     * 
     */
    public void setEventRef(long value) {
        this.eventRef = value;
    }

    /**
     * Ruft den Wert der status-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeStatus }
     *     
     */
    public TypeStatus getStatus() {
        return status;
    }

    /**
     * Legt den Wert der status-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeStatus }
     *     
     */
    public void setStatus(TypeStatus value) {
        this.status = value;
    }

}
