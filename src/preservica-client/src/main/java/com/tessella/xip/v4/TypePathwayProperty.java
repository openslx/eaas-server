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
 * A measured property of a File, before and after migration, that should be maintained unchanged, within a set tolerance.
 * 
 * <p>Java-Klasse für typePathwayProperty complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typePathwayProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OriginalProperty" type="{http://www.tessella.com/XIP/v4}typeProperty"/>
 *         &lt;element name="TargetProperty" type="{http://www.tessella.com/XIP/v4}typeProperty"/>
 *         &lt;element name="Variance" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.tessella.com/XIP/v4}typeReference" />
 *       &lt;attribute name="approved" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typePathwayProperty", propOrder = {
    "originalProperty",
    "targetProperty",
    "variance"
})
public class TypePathwayProperty {

    @XmlElement(name = "OriginalProperty", required = true)
    protected TypeProperty originalProperty;
    @XmlElement(name = "TargetProperty", required = true)
    protected TypeProperty targetProperty;
    @XmlElement(name = "Variance")
    protected double variance;
    @XmlAttribute(name = "id", required = true)
    protected long id;
    @XmlAttribute(name = "approved")
    protected Boolean approved;

    /**
     * Ruft den Wert der originalProperty-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeProperty }
     *     
     */
    public TypeProperty getOriginalProperty() {
        return originalProperty;
    }

    /**
     * Legt den Wert der originalProperty-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeProperty }
     *     
     */
    public void setOriginalProperty(TypeProperty value) {
        this.originalProperty = value;
    }

    /**
     * Ruft den Wert der targetProperty-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeProperty }
     *     
     */
    public TypeProperty getTargetProperty() {
        return targetProperty;
    }

    /**
     * Legt den Wert der targetProperty-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeProperty }
     *     
     */
    public void setTargetProperty(TypeProperty value) {
        this.targetProperty = value;
    }

    /**
     * Ruft den Wert der variance-Eigenschaft ab.
     * 
     */
    public double getVariance() {
        return variance;
    }

    /**
     * Legt den Wert der variance-Eigenschaft fest.
     * 
     */
    public void setVariance(double value) {
        this.variance = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der approved-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isApproved() {
        if (approved == null) {
            return false;
        } else {
            return approved;
        }
    }

    /**
     * Legt den Wert der approved-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApproved(Boolean value) {
        this.approved = value;
    }

}
