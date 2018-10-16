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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * A deprecated Digital File that is associated with a Manifestation. This entity stores historic information on a Digital File associated with a manifestation that has since been superceded by a newer Digital File.
 * 
 * <p>Java-Klasse für typeDeprecatedManifestationFile complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeDeprecatedManifestationFile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FileRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DeprecatedBy" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="DeprecatedOn" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *       &lt;attribute name="order" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="status" use="required" type="{http://www.tessella.com/XIP/v4}typeStatus" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeDeprecatedManifestationFile", propOrder = {
    "fileRef",
    "path",
    "deprecatedBy",
    "deprecatedOn"
})
public class TypeDeprecatedManifestationFile {

    @XmlElement(name = "FileRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String fileRef;
    @XmlElement(name = "Path", required = true)
    protected String path;
    @XmlElement(name = "DeprecatedBy", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String deprecatedBy;
    @XmlElement(name = "DeprecatedOn", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar deprecatedOn;
    @XmlAttribute(name = "order")
    protected Long order;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der fileRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileRef() {
        return fileRef;
    }

    /**
     * Legt den Wert der fileRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileRef(String value) {
        this.fileRef = value;
    }

    /**
     * Ruft den Wert der path-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Legt den Wert der path-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Ruft den Wert der deprecatedBy-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeprecatedBy() {
        return deprecatedBy;
    }

    /**
     * Legt den Wert der deprecatedBy-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeprecatedBy(String value) {
        this.deprecatedBy = value;
    }

    /**
     * Ruft den Wert der deprecatedOn-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDeprecatedOn() {
        return deprecatedOn;
    }

    /**
     * Legt den Wert der deprecatedOn-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDeprecatedOn(XMLGregorianCalendar value) {
        this.deprecatedOn = value;
    }

    /**
     * Ruft den Wert der order-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOrder() {
        return order;
    }

    /**
     * Legt den Wert der order-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOrder(Long value) {
        this.order = value;
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
