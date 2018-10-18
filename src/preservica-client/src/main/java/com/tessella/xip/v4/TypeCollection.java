//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.16 um 03:18:14 PM CET 
//


package com.tessella.xip.v4;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * A hierarchical grouping of Deliverable Units (or potential Deliverable Units) for the purposes of ordering content, such as by theme or topic.N.B. A Collection can exist even if no Deliverable Units are held within it yet. Note that a Collection can either contain child collections, or has deliverable units associated with it, but not a mixture of the two, although this is not enforced by the schema.
 * 
 * <p>Java-Klasse für typeCollection complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeCollection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CollectionRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="CollectionTypeRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="ParentRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="CollectionCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ClosureStatus" type="{http://www.tessella.com/XIP/v4}typeClosureStatus" minOccurs="0"/>
 *         &lt;element name="ClosureType" type="{http://www.tessella.com/XIP/v4}typeClosureType" minOccurs="0"/>
 *         &lt;element name="ClosureCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ClassNumber" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="SubclassNumber" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="AccessReviewEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
 *         &lt;element name="CustodialHistory" type="{http://www.tessella.com/XIP/v4}arbitraryContents" minOccurs="0"/>
 *         &lt;element name="PreservationHistory" type="{http://www.tessella.com/XIP/v4}arbitraryContents" minOccurs="0"/>
 *         &lt;element name="SecurityTag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Metadata" type="{http://www.tessella.com/XIP/v4}genericMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="BaseVersion" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="CurrentVersion" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="Identifier" type="{http://www.tessella.com/XIP/v4}typeIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Virtual" type="{http://www.w3.org/2001/XMLSchema}short" minOccurs="0"/>
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
@XmlType(name = "typeCollection", propOrder = {
    "collectionRef",
    "collectionTypeRef",
    "parentRef",
    "collectionCode",
    "title",
    "closureStatus",
    "closureType",
    "closureCode",
    "classNumber",
    "subclassNumber",
    "accessReviewEvent",
    "custodialHistory",
    "preservationHistory",
    "securityTag",
    "metadata",
    "baseVersion",
    "currentVersion",
    "identifier",
    "virtual"
})
public class TypeCollection {

    @XmlElement(name = "CollectionRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String collectionRef;
    @XmlElement(name = "CollectionTypeRef")
    protected long collectionTypeRef;
    @XmlElement(name = "ParentRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String parentRef;
    @XmlElement(name = "CollectionCode")
    protected String collectionCode;
    @XmlElement(name = "Title", required = true)
    protected String title;
    @XmlElement(name = "ClosureStatus")
    @XmlSchemaType(name = "NMTOKEN")
    protected TypeClosureStatus closureStatus;
    @XmlElement(name = "ClosureType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String closureType;
    @XmlElement(name = "ClosureCode")
    protected String closureCode;
    @XmlElement(name = "ClassNumber")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger classNumber;
    @XmlElement(name = "SubclassNumber")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger subclassNumber;
    @XmlElement(name = "AccessReviewEvent")
    protected TypeEvent accessReviewEvent;
    @XmlElement(name = "CustodialHistory")
    protected ArbitraryContents custodialHistory;
    @XmlElement(name = "PreservationHistory")
    protected ArbitraryContents preservationHistory;
    @XmlElement(name = "SecurityTag", defaultValue = "open")
    protected String securityTag;
    @XmlElement(name = "Metadata")
    protected List<GenericMetadata> metadata;
    @XmlElement(name = "BaseVersion")
    @XmlSchemaType(name = "anySimpleType")
    protected String baseVersion;
    @XmlElement(name = "CurrentVersion")
    @XmlSchemaType(name = "anySimpleType")
    protected String currentVersion;
    @XmlElement(name = "Identifier")
    protected List<TypeIdentifier> identifier;
    @XmlElement(name = "Virtual")
    protected Short virtual;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der collectionRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionRef() {
        return collectionRef;
    }

    /**
     * Legt den Wert der collectionRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionRef(String value) {
        this.collectionRef = value;
    }

    /**
     * Ruft den Wert der collectionTypeRef-Eigenschaft ab.
     * 
     */
    public long getCollectionTypeRef() {
        return collectionTypeRef;
    }

    /**
     * Legt den Wert der collectionTypeRef-Eigenschaft fest.
     * 
     */
    public void setCollectionTypeRef(long value) {
        this.collectionTypeRef = value;
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
     * Ruft den Wert der collectionCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionCode() {
        return collectionCode;
    }

    /**
     * Legt den Wert der collectionCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionCode(String value) {
        this.collectionCode = value;
    }

    /**
     * Ruft den Wert der title-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Legt den Wert der title-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Ruft den Wert der closureStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeClosureStatus }
     *     
     */
    public TypeClosureStatus getClosureStatus() {
        return closureStatus;
    }

    /**
     * Legt den Wert der closureStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeClosureStatus }
     *     
     */
    public void setClosureStatus(TypeClosureStatus value) {
        this.closureStatus = value;
    }

    /**
     * Ruft den Wert der closureType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClosureType() {
        return closureType;
    }

    /**
     * Legt den Wert der closureType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClosureType(String value) {
        this.closureType = value;
    }

    /**
     * Ruft den Wert der closureCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClosureCode() {
        return closureCode;
    }

    /**
     * Legt den Wert der closureCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClosureCode(String value) {
        this.closureCode = value;
    }

    /**
     * Ruft den Wert der classNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getClassNumber() {
        return classNumber;
    }

    /**
     * Legt den Wert der classNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setClassNumber(BigInteger value) {
        this.classNumber = value;
    }

    /**
     * Ruft den Wert der subclassNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSubclassNumber() {
        return subclassNumber;
    }

    /**
     * Legt den Wert der subclassNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSubclassNumber(BigInteger value) {
        this.subclassNumber = value;
    }

    /**
     * Ruft den Wert der accessReviewEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getAccessReviewEvent() {
        return accessReviewEvent;
    }

    /**
     * Legt den Wert der accessReviewEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setAccessReviewEvent(TypeEvent value) {
        this.accessReviewEvent = value;
    }

    /**
     * Ruft den Wert der custodialHistory-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArbitraryContents }
     *     
     */
    public ArbitraryContents getCustodialHistory() {
        return custodialHistory;
    }

    /**
     * Legt den Wert der custodialHistory-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArbitraryContents }
     *     
     */
    public void setCustodialHistory(ArbitraryContents value) {
        this.custodialHistory = value;
    }

    /**
     * Ruft den Wert der preservationHistory-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArbitraryContents }
     *     
     */
    public ArbitraryContents getPreservationHistory() {
        return preservationHistory;
    }

    /**
     * Legt den Wert der preservationHistory-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArbitraryContents }
     *     
     */
    public void setPreservationHistory(ArbitraryContents value) {
        this.preservationHistory = value;
    }

    /**
     * Ruft den Wert der securityTag-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurityTag() {
        return securityTag;
    }

    /**
     * Legt den Wert der securityTag-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurityTag(String value) {
        this.securityTag = value;
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
     * Ruft den Wert der virtual-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getVirtual() {
        return virtual;
    }

    /**
     * Legt den Wert der virtual-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setVirtual(Short value) {
        this.virtual = value;
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
