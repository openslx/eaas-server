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
 * The format of a Digital File; information comes from the Technical Registry.
 * 
 * <p>Java-Klasse für typeFileFormat complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeFileFormat">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FormatRisk" type="{http://www.tessella.com/XIP/v4}typeRiskValue"/>
 *         &lt;element name="FormatInfo" type="{http://www.tessella.com/XIP/v4}typeFormatInfo"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeFileFormat", propOrder = {
    "formatRisk",
    "formatInfo"
})
public class TypeFileFormat {

    @XmlElement(name = "FormatRisk")
    protected float formatRisk;
    @XmlElement(name = "FormatInfo", required = true)
    protected TypeFormatInfo formatInfo;

    /**
     * Ruft den Wert der formatRisk-Eigenschaft ab.
     * 
     */
    public float getFormatRisk() {
        return formatRisk;
    }

    /**
     * Legt den Wert der formatRisk-Eigenschaft fest.
     * 
     */
    public void setFormatRisk(float value) {
        this.formatRisk = value;
    }

    /**
     * Ruft den Wert der formatInfo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeFormatInfo }
     *     
     */
    public TypeFormatInfo getFormatInfo() {
        return formatInfo;
    }

    /**
     * Legt den Wert der formatInfo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeFormatInfo }
     *     
     */
    public void setFormatInfo(TypeFormatInfo value) {
        this.formatInfo = value;
    }

}
