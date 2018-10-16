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


/**
 * A defined action which uses a (software) tool to transform a file from a format at risk to a chosen target format.
 * 
 * <p>Java-Klasse für typeMigrationPathway complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeMigrationPathway">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MigrationPathwayRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="PathwayType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PathwayStatus" type="{http://www.tessella.com/XIP/v4}typePathwayStatus"/>
 *         &lt;element name="TargetComponentManifestationType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OriginalFormat" type="{http://www.tessella.com/XIP/v4}typeFileFormat"/>
 *         &lt;element name="TargetFormat" type="{http://www.tessella.com/XIP/v4}typeFileFormat" minOccurs="0"/>
 *         &lt;element name="Tool" type="{http://www.tessella.com/XIP/v4}typeTool"/>
 *         &lt;element name="PathwayProperties" type="{http://www.tessella.com/XIP/v4}typePathwayProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="current" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeMigrationPathway", propOrder = {
    "migrationPathwayRef",
    "pathwayType",
    "pathwayStatus",
    "targetComponentManifestationType",
    "originalFormat",
    "targetFormat",
    "tool",
    "pathwayProperties"
})
public class TypeMigrationPathway {

    @XmlElement(name = "MigrationPathwayRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String migrationPathwayRef;
    @XmlElement(name = "PathwayType", required = true)
    protected String pathwayType;
    @XmlElement(name = "PathwayStatus", required = true)
    protected TypePathwayStatus pathwayStatus;
    @XmlElement(name = "TargetComponentManifestationType")
    protected String targetComponentManifestationType;
    @XmlElement(name = "OriginalFormat", required = true)
    protected TypeFileFormat originalFormat;
    @XmlElement(name = "TargetFormat")
    protected TypeFileFormat targetFormat;
    @XmlElement(name = "Tool", required = true)
    protected TypeTool tool;
    @XmlElement(name = "PathwayProperties")
    protected TypePathwayProperties pathwayProperties;
    @XmlAttribute(name = "current")
    protected Boolean current;

    /**
     * Ruft den Wert der migrationPathwayRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMigrationPathwayRef() {
        return migrationPathwayRef;
    }

    /**
     * Legt den Wert der migrationPathwayRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMigrationPathwayRef(String value) {
        this.migrationPathwayRef = value;
    }

    /**
     * Ruft den Wert der pathwayType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPathwayType() {
        return pathwayType;
    }

    /**
     * Legt den Wert der pathwayType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPathwayType(String value) {
        this.pathwayType = value;
    }

    /**
     * Ruft den Wert der pathwayStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypePathwayStatus }
     *     
     */
    public TypePathwayStatus getPathwayStatus() {
        return pathwayStatus;
    }

    /**
     * Legt den Wert der pathwayStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypePathwayStatus }
     *     
     */
    public void setPathwayStatus(TypePathwayStatus value) {
        this.pathwayStatus = value;
    }

    /**
     * Ruft den Wert der targetComponentManifestationType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetComponentManifestationType() {
        return targetComponentManifestationType;
    }

    /**
     * Legt den Wert der targetComponentManifestationType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetComponentManifestationType(String value) {
        this.targetComponentManifestationType = value;
    }

    /**
     * Ruft den Wert der originalFormat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeFileFormat }
     *     
     */
    public TypeFileFormat getOriginalFormat() {
        return originalFormat;
    }

    /**
     * Legt den Wert der originalFormat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeFileFormat }
     *     
     */
    public void setOriginalFormat(TypeFileFormat value) {
        this.originalFormat = value;
    }

    /**
     * Ruft den Wert der targetFormat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeFileFormat }
     *     
     */
    public TypeFileFormat getTargetFormat() {
        return targetFormat;
    }

    /**
     * Legt den Wert der targetFormat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeFileFormat }
     *     
     */
    public void setTargetFormat(TypeFileFormat value) {
        this.targetFormat = value;
    }

    /**
     * Ruft den Wert der tool-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeTool }
     *     
     */
    public TypeTool getTool() {
        return tool;
    }

    /**
     * Legt den Wert der tool-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeTool }
     *     
     */
    public void setTool(TypeTool value) {
        this.tool = value;
    }

    /**
     * Ruft den Wert der pathwayProperties-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypePathwayProperties }
     *     
     */
    public TypePathwayProperties getPathwayProperties() {
        return pathwayProperties;
    }

    /**
     * Legt den Wert der pathwayProperties-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypePathwayProperties }
     *     
     */
    public void setPathwayProperties(TypePathwayProperties value) {
        this.pathwayProperties = value;
    }

    /**
     * Ruft den Wert der current-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCurrent() {
        if (current == null) {
            return false;
        } else {
            return current;
        }
    }

    /**
     * Legt den Wert der current-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCurrent(Boolean value) {
        this.current = value;
    }

}
