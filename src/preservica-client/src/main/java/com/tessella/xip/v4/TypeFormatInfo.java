//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.16 um 03:18:14 PM CET 
//


package com.tessella.xip.v4;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Holds information about the format of a Digital File, including PUID information from the Technical Registry.
 * 
 * <p>Java-Klasse für typeFormatInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeFormatInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FormatPUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FormatName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FormatVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Valid" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Warning" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeFormatInfo", propOrder = {
    "formatPUID",
    "formatName",
    "formatVersion",
    "valid",
    "warning"
})
public class TypeFormatInfo {

    @XmlElement(name = "FormatPUID", required = true)
    protected String formatPUID;
    @XmlElement(name = "FormatName", required = true)
    protected String formatName;
    @XmlElement(name = "FormatVersion")
    protected String formatVersion;
    @XmlElement(name = "Valid")
    protected Boolean valid;
    @XmlElement(name = "Warning")
    protected List<String> warning;

    /**
     * Ruft den Wert der formatPUID-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatPUID() {
        return formatPUID;
    }

    /**
     * Legt den Wert der formatPUID-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatPUID(String value) {
        this.formatPUID = value;
    }

    /**
     * Ruft den Wert der formatName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatName() {
        return formatName;
    }

    /**
     * Legt den Wert der formatName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatName(String value) {
        this.formatName = value;
    }

    /**
     * Ruft den Wert der formatVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatVersion() {
        return formatVersion;
    }

    /**
     * Legt den Wert der formatVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatVersion(String value) {
        this.formatVersion = value;
    }

    /**
     * Ruft den Wert der valid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isValid() {
        return valid;
    }

    /**
     * Legt den Wert der valid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setValid(Boolean value) {
        this.valid = value;
    }

    /**
     * Gets the value of the warning property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the warning property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWarning().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getWarning() {
        if (warning == null) {
            warning = new ArrayList<String>();
        }
        return this.warning;
    }

}
