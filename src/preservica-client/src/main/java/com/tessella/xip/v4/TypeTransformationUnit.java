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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * A Component Manifestation that is transformed via migration.  A Transformation Unit keeps track of the set of formats that are migrated within this Component (and hence can calculate the files which are transformed and those that are unchanged as a result of migration).  For each format to be migrated, it includes details of the migration to perform.  This includes details of the Migration Pathway to use.
 * N.B. If a Component Manifestation is being transformed and some files are not part of the migration then these unchanged files will be carried forward into the new manifestation.  The new Component Manifestation will consist of these unchanged files plus the Post-Transformation File Set.
 * 
 * <p>Java-Klasse für typeTransformationUnit complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeTransformationUnit">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TransformationUnitRef" type="{http://www.tessella.com/XIP/v4}typeGUID"/>
 *         &lt;element name="MigrationPathwayRefs" type="{http://www.tessella.com/XIP/v4}typeMigrationPathwayRef" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ParentRef" type="{http://www.tessella.com/XIP/v4}typeGUID" minOccurs="0"/>
 *         &lt;element name="SourceManifestationRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="TargetManifestationRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="SourceComponentManifestationRef" type="{http://www.tessella.com/XIP/v4}typeGUID" minOccurs="0"/>
 *         &lt;element name="PostTransformationFileSetRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="TransformationEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
 *         &lt;element name="FormatsAtRisk" type="{http://www.tessella.com/XIP/v4}typeFormatsAtRisk" minOccurs="0"/>
 *         &lt;element name="FilesAtRisk" type="{http://www.tessella.com/XIP/v4}typeFilesAtRisk" minOccurs="0"/>
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
@XmlType(name = "typeTransformationUnit", propOrder = {
    "transformationUnitRef",
    "migrationPathwayRefs",
    "parentRef",
    "sourceManifestationRef",
    "targetManifestationRef",
    "sourceComponentManifestationRef",
    "postTransformationFileSetRef",
    "transformationEvent",
    "formatsAtRisk",
    "filesAtRisk"
})
public class TypeTransformationUnit {

    @XmlElement(name = "TransformationUnitRef", required = true)
    protected String transformationUnitRef;
    @XmlElement(name = "MigrationPathwayRefs")
    protected List<TypeMigrationPathwayRef> migrationPathwayRefs;
    @XmlElement(name = "ParentRef")
    protected String parentRef;
    @XmlElement(name = "SourceManifestationRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String sourceManifestationRef;
    @XmlElement(name = "TargetManifestationRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String targetManifestationRef;
    @XmlElement(name = "SourceComponentManifestationRef")
    protected String sourceComponentManifestationRef;
    @XmlElement(name = "PostTransformationFileSetRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String postTransformationFileSetRef;
    @XmlElement(name = "TransformationEvent")
    protected TypeEvent transformationEvent;
    @XmlElement(name = "FormatsAtRisk")
    protected TypeFormatsAtRisk formatsAtRisk;
    @XmlElement(name = "FilesAtRisk")
    protected TypeFilesAtRisk filesAtRisk;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der transformationUnitRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransformationUnitRef() {
        return transformationUnitRef;
    }

    /**
     * Legt den Wert der transformationUnitRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransformationUnitRef(String value) {
        this.transformationUnitRef = value;
    }

    /**
     * Gets the value of the migrationPathwayRefs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the migrationPathwayRefs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMigrationPathwayRefs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeMigrationPathwayRef }
     * 
     * 
     */
    public List<TypeMigrationPathwayRef> getMigrationPathwayRefs() {
        if (migrationPathwayRefs == null) {
            migrationPathwayRefs = new ArrayList<TypeMigrationPathwayRef>();
        }
        return this.migrationPathwayRefs;
    }

    /**
     * Ruft den Wert der parentRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentRef() {
        return parentRef;
    }

    /**
     * Legt den Wert der parentRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentRef(String value) {
        this.parentRef = value;
    }

    /**
     * Ruft den Wert der sourceManifestationRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceManifestationRef() {
        return sourceManifestationRef;
    }

    /**
     * Legt den Wert der sourceManifestationRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceManifestationRef(String value) {
        this.sourceManifestationRef = value;
    }

    /**
     * Ruft den Wert der targetManifestationRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetManifestationRef() {
        return targetManifestationRef;
    }

    /**
     * Legt den Wert der targetManifestationRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetManifestationRef(String value) {
        this.targetManifestationRef = value;
    }

    /**
     * Ruft den Wert der sourceComponentManifestationRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceComponentManifestationRef() {
        return sourceComponentManifestationRef;
    }

    /**
     * Legt den Wert der sourceComponentManifestationRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceComponentManifestationRef(String value) {
        this.sourceComponentManifestationRef = value;
    }

    /**
     * Ruft den Wert der postTransformationFileSetRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostTransformationFileSetRef() {
        return postTransformationFileSetRef;
    }

    /**
     * Legt den Wert der postTransformationFileSetRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostTransformationFileSetRef(String value) {
        this.postTransformationFileSetRef = value;
    }

    /**
     * Ruft den Wert der transformationEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getTransformationEvent() {
        return transformationEvent;
    }

    /**
     * Legt den Wert der transformationEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setTransformationEvent(TypeEvent value) {
        this.transformationEvent = value;
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

    /**
     * Ruft den Wert der filesAtRisk-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeFilesAtRisk }
     *     
     */
    public TypeFilesAtRisk getFilesAtRisk() {
        return filesAtRisk;
    }

    /**
     * Legt den Wert der filesAtRisk-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeFilesAtRisk }
     *     
     */
    public void setFilesAtRisk(TypeFilesAtRisk value) {
        this.filesAtRisk = value;
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
