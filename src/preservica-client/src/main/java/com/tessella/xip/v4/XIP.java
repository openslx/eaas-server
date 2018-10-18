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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Control" type="{http://www.tessella.com/XIP/v4}arbitraryContents" minOccurs="0"/>
 *         &lt;element name="Collections" type="{http://www.tessella.com/XIP/v4}typeCollections" minOccurs="0"/>
 *         &lt;element name="Aggregations" type="{http://www.tessella.com/XIP/v4}typeAggregations" minOccurs="0"/>
 *         &lt;element name="DeliverableUnits" type="{http://www.tessella.com/XIP/v4}typeDeliverableUnits" minOccurs="0"/>
 *         &lt;element name="Files" type="{http://www.tessella.com/XIP/v4}typeFiles" minOccurs="0"/>
 *         &lt;element name="Transformations" type="{http://www.tessella.com/XIP/v4}typeTransformations" minOccurs="0"/>
 *         &lt;element name="IngestedFileSets" type="{http://www.tessella.com/XIP/v4}typeIngestedFileSets" minOccurs="0"/>
 *         &lt;element name="MigrationPathways" type="{http://www.tessella.com/XIP/v4}typeMigrationPathways" minOccurs="0"/>
 *         &lt;element name="FormatsAtRisk" type="{http://www.tessella.com/XIP/v4}typeFormatsAtRisk" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "control",
    "collections",
    "aggregations",
    "deliverableUnits",
    "files",
    "transformations",
    "ingestedFileSets",
    "migrationPathways",
    "formatsAtRisk"
})
@XmlRootElement(name = "XIP")
public class XIP {

    @XmlElement(name = "Control")
    protected ArbitraryContents control;
    @XmlElement(name = "Collections")
    protected TypeCollections collections;
    @XmlElement(name = "Aggregations")
    protected TypeAggregations aggregations;
    @XmlElement(name = "DeliverableUnits")
    protected TypeDeliverableUnits deliverableUnits;
    @XmlElement(name = "Files")
    protected TypeFiles files;
    @XmlElement(name = "Transformations")
    protected TypeTransformations transformations;
    @XmlElement(name = "IngestedFileSets")
    protected TypeIngestedFileSets ingestedFileSets;
    @XmlElement(name = "MigrationPathways")
    protected TypeMigrationPathways migrationPathways;
    @XmlElement(name = "FormatsAtRisk")
    protected TypeFormatsAtRisk formatsAtRisk;

    /**
     * Ruft den Wert der control-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArbitraryContents }
     *     
     */
    public ArbitraryContents getControl() {
        return control;
    }

    /**
     * Legt den Wert der control-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArbitraryContents }
     *     
     */
    public void setControl(ArbitraryContents value) {
        this.control = value;
    }

    /**
     * Ruft den Wert der collections-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeCollections }
     *     
     */
    public TypeCollections getCollections() {
        return collections;
    }

    /**
     * Legt den Wert der collections-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeCollections }
     *     
     */
    public void setCollections(TypeCollections value) {
        this.collections = value;
    }

    /**
     * Ruft den Wert der aggregations-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeAggregations }
     *     
     */
    public TypeAggregations getAggregations() {
        return aggregations;
    }

    /**
     * Legt den Wert der aggregations-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeAggregations }
     *     
     */
    public void setAggregations(TypeAggregations value) {
        this.aggregations = value;
    }

    /**
     * Ruft den Wert der deliverableUnits-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeDeliverableUnits }
     *     
     */
    public TypeDeliverableUnits getDeliverableUnits() {
        return deliverableUnits;
    }

    /**
     * Legt den Wert der deliverableUnits-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeDeliverableUnits }
     *     
     */
    public void setDeliverableUnits(TypeDeliverableUnits value) {
        this.deliverableUnits = value;
    }

    /**
     * Ruft den Wert der files-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeFiles }
     *     
     */
    public TypeFiles getFiles() {
        return files;
    }

    /**
     * Legt den Wert der files-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeFiles }
     *     
     */
    public void setFiles(TypeFiles value) {
        this.files = value;
    }

    /**
     * Ruft den Wert der transformations-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeTransformations }
     *     
     */
    public TypeTransformations getTransformations() {
        return transformations;
    }

    /**
     * Legt den Wert der transformations-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeTransformations }
     *     
     */
    public void setTransformations(TypeTransformations value) {
        this.transformations = value;
    }

    /**
     * Ruft den Wert der ingestedFileSets-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeIngestedFileSets }
     *     
     */
    public TypeIngestedFileSets getIngestedFileSets() {
        return ingestedFileSets;
    }

    /**
     * Legt den Wert der ingestedFileSets-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeIngestedFileSets }
     *     
     */
    public void setIngestedFileSets(TypeIngestedFileSets value) {
        this.ingestedFileSets = value;
    }

    /**
     * Ruft den Wert der migrationPathways-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeMigrationPathways }
     *     
     */
    public TypeMigrationPathways getMigrationPathways() {
        return migrationPathways;
    }

    /**
     * Legt den Wert der migrationPathways-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeMigrationPathways }
     *     
     */
    public void setMigrationPathways(TypeMigrationPathways value) {
        this.migrationPathways = value;
    }

    /**
     * Ruft den Wert der formatsAtRisk-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeFormatsAtRisk }
     *     
     */
    public TypeFormatsAtRisk getFormatsAtRisk() {
        return formatsAtRisk;
    }

    /**
     * Legt den Wert der formatsAtRisk-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeFormatsAtRisk }
     *     
     */
    public void setFormatsAtRisk(TypeFormatsAtRisk value) {
        this.formatsAtRisk = value;
    }

}
