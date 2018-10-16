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
 * A Data Object that embodies a Deliverable Unit in one or more technical environments (particular set of technologies).  This is an instance of a deliverable unit in a particular technology (i.e. the collection of files that allow authentic rendering of the deliverable unit within a given technical environment).
 * 
 * <p>Java-Klasse für typeManifestation complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeManifestation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DeliverableUnitRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="ManifestationRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="ManifestationRelRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="Originality" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Active" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="TypeRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="TechnicalEnvironmentRef" type="{http://www.tessella.com/XIP/v4}typeReference" minOccurs="0"/>
 *         &lt;element name="TechnicalEnvironmentDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Metadata" type="{http://www.tessella.com/XIP/v4}genericMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ComponentManifestation" type="{http://www.tessella.com/XIP/v4}typeComponentManifestation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ManifestationFile" type="{http://www.tessella.com/XIP/v4}typeManifestationFile" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="BaseVersion" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="CurrentVersion" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="Identifier" type="{http://www.tessella.com/XIP/v4}typeIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="MetadataPresentable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ContentPresentable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="DeprecatedManifestationFile" type="{http://www.tessella.com/XIP/v4}typeDeprecatedManifestationFile" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="GroupRef" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "typeManifestation", propOrder = {
    "deliverableUnitRef",
    "manifestationRef",
    "manifestationRelRef",
    "originality",
    "active",
    "typeRef",
    "technicalEnvironmentRef",
    "technicalEnvironmentDescription",
    "metadata",
    "componentManifestation",
    "manifestationFile",
    "baseVersion",
    "currentVersion",
    "identifier",
    "metadataPresentable",
    "contentPresentable",
    "deprecatedManifestationFile",
    "groupRef"
})
public class TypeManifestation {

    @XmlElement(name = "DeliverableUnitRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String deliverableUnitRef;
    @XmlElement(name = "ManifestationRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String manifestationRef;
    @XmlElement(name = "ManifestationRelRef")
    protected long manifestationRelRef;
    @XmlElement(name = "Originality")
    protected boolean originality;
    @XmlElement(name = "Active")
    protected boolean active;
    @XmlElement(name = "TypeRef")
    protected long typeRef;
    @XmlElement(name = "TechnicalEnvironmentRef")
    protected Long technicalEnvironmentRef;
    @XmlElement(name = "TechnicalEnvironmentDescription")
    protected String technicalEnvironmentDescription;
    @XmlElement(name = "Metadata")
    protected List<GenericMetadata> metadata;
    @XmlElement(name = "ComponentManifestation")
    protected List<TypeComponentManifestation> componentManifestation;
    @XmlElement(name = "ManifestationFile")
    protected List<TypeManifestationFile> manifestationFile;
    @XmlElement(name = "BaseVersion")
    @XmlSchemaType(name = "anySimpleType")
    protected String baseVersion;
    @XmlElement(name = "CurrentVersion")
    @XmlSchemaType(name = "anySimpleType")
    protected String currentVersion;
    @XmlElement(name = "Identifier")
    protected List<TypeIdentifier> identifier;
    @XmlElement(name = "MetadataPresentable")
    protected Boolean metadataPresentable;
    @XmlElement(name = "ContentPresentable")
    protected Boolean contentPresentable;
    @XmlElement(name = "DeprecatedManifestationFile")
    protected List<TypeDeprecatedManifestationFile> deprecatedManifestationFile;
    @XmlElement(name = "GroupRef")
    protected String groupRef;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der deliverableUnitRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeliverableUnitRef() {
        return deliverableUnitRef;
    }

    /**
     * Legt den Wert der deliverableUnitRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeliverableUnitRef(String value) {
        this.deliverableUnitRef = value;
    }

    /**
     * Ruft den Wert der manifestationRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManifestationRef() {
        return manifestationRef;
    }

    /**
     * Legt den Wert der manifestationRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManifestationRef(String value) {
        this.manifestationRef = value;
    }

    /**
     * Ruft den Wert der manifestationRelRef-Eigenschaft ab.
     * 
     */
    public long getManifestationRelRef() {
        return manifestationRelRef;
    }

    /**
     * Legt den Wert der manifestationRelRef-Eigenschaft fest.
     * 
     */
    public void setManifestationRelRef(long value) {
        this.manifestationRelRef = value;
    }

    /**
     * Ruft den Wert der originality-Eigenschaft ab.
     * 
     */
    public boolean isOriginality() {
        return originality;
    }

    /**
     * Legt den Wert der originality-Eigenschaft fest.
     * 
     */
    public void setOriginality(boolean value) {
        this.originality = value;
    }

    /**
     * Ruft den Wert der active-Eigenschaft ab.
     * 
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Legt den Wert der active-Eigenschaft fest.
     * 
     */
    public void setActive(boolean value) {
        this.active = value;
    }

    /**
     * Ruft den Wert der typeRef-Eigenschaft ab.
     * 
     */
    public long getTypeRef() {
        return typeRef;
    }

    /**
     * Legt den Wert der typeRef-Eigenschaft fest.
     * 
     */
    public void setTypeRef(long value) {
        this.typeRef = value;
    }

    /**
     * Ruft den Wert der technicalEnvironmentRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTechnicalEnvironmentRef() {
        return technicalEnvironmentRef;
    }

    /**
     * Legt den Wert der technicalEnvironmentRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTechnicalEnvironmentRef(Long value) {
        this.technicalEnvironmentRef = value;
    }

    /**
     * Ruft den Wert der technicalEnvironmentDescription-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTechnicalEnvironmentDescription() {
        return technicalEnvironmentDescription;
    }

    /**
     * Legt den Wert der technicalEnvironmentDescription-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTechnicalEnvironmentDescription(String value) {
        this.technicalEnvironmentDescription = value;
    }

    /**
     * Gets the value of the metadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenericMetadata }
     * 
     * 
     */
    public List<GenericMetadata> getMetadata() {
        if (metadata == null) {
            metadata = new ArrayList<GenericMetadata>();
        }
        return this.metadata;
    }

    /**
     * Gets the value of the componentManifestation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the componentManifestation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComponentManifestation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeComponentManifestation }
     * 
     * 
     */
    public List<TypeComponentManifestation> getComponentManifestation() {
        if (componentManifestation == null) {
            componentManifestation = new ArrayList<TypeComponentManifestation>();
        }
        return this.componentManifestation;
    }

    /**
     * Gets the value of the manifestationFile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the manifestationFile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManifestationFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeManifestationFile }
     * 
     * 
     */
    public List<TypeManifestationFile> getManifestationFile() {
        if (manifestationFile == null) {
            manifestationFile = new ArrayList<TypeManifestationFile>();
        }
        return this.manifestationFile;
    }

    /**
     * Ruft den Wert der baseVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseVersion() {
        return baseVersion;
    }

    /**
     * Legt den Wert der baseVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseVersion(String value) {
        this.baseVersion = value;
    }

    /**
     * Ruft den Wert der currentVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Legt den Wert der currentVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrentVersion(String value) {
        this.currentVersion = value;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the identifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeIdentifier }
     * 
     * 
     */
    public List<TypeIdentifier> getIdentifier() {
        if (identifier == null) {
            identifier = new ArrayList<TypeIdentifier>();
        }
        return this.identifier;
    }

    /**
     * Ruft den Wert der metadataPresentable-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMetadataPresentable() {
        return metadataPresentable;
    }

    /**
     * Legt den Wert der metadataPresentable-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMetadataPresentable(Boolean value) {
        this.metadataPresentable = value;
    }

    /**
     * Ruft den Wert der contentPresentable-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isContentPresentable() {
        return contentPresentable;
    }

    /**
     * Legt den Wert der contentPresentable-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContentPresentable(Boolean value) {
        this.contentPresentable = value;
    }

    /**
     * Gets the value of the deprecatedManifestationFile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deprecatedManifestationFile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeprecatedManifestationFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeDeprecatedManifestationFile }
     * 
     * 
     */
    public List<TypeDeprecatedManifestationFile> getDeprecatedManifestationFile() {
        if (deprecatedManifestationFile == null) {
            deprecatedManifestationFile = new ArrayList<TypeDeprecatedManifestationFile>();
        }
        return this.deprecatedManifestationFile;
    }

    /**
     * Ruft den Wert der groupRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupRef() {
        return groupRef;
    }

    /**
     * Legt den Wert der groupRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupRef(String value) {
        this.groupRef = value;
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
