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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * A reference to a specific Migration Pathway.
 * 
 * <p>Java-Klasse für typeMigrationPathwayRef complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeMigrationPathwayRef">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MigrationPathwayRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="PathwayType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeMigrationPathwayRef", propOrder = {
    "migrationPathwayRef",
    "pathwayType"
})
public class TypeMigrationPathwayRef {

    @XmlElement(name = "MigrationPathwayRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String migrationPathwayRef;
    @XmlElement(name = "PathwayType", required = true)
    protected String pathwayType;

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

}
