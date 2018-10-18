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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * An Information Object that is the conceptual entity (a record) that is delivered by archives and libraries.
 * 
 * <p>Java-Klasse für typeDeliverableUnit complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeDeliverableUnit">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DeliverableUnitRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;choice>
 *           &lt;element name="CollectionRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *           &lt;element name="RecordSeriesRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;/choice>
 *         &lt;element name="AccessionRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="AccumulationRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="ParentRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="DigitalSurrogate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CatalogueReference" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ScopeAndContent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ClosureStatus" type="{http://www.tessella.com/XIP/v4}typeClosureStatus" minOccurs="0"/>
 *         &lt;element name="ClosureType" type="{http://www.tessella.com/XIP/v4}typeClosureType" minOccurs="0"/>
 *         &lt;element name="ClosureCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CoverageFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="CoverageTo" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CreatorCorporateBodyRef" type="{http://www.tessella.com/XIP/v4}typeReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="CreatorPersonRef" type="{http://www.tessella.com/XIP/v4}typeReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="CorporateBodyRef" type="{http://www.tessella.com/XIP/v4}typeReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PersonRef" type="{http://www.tessella.com/XIP/v4}typeReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SubjectRef" type="{http://www.tessella.com/XIP/v4}typeReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PlaceRef" type="{http://www.tessella.com/XIP/v4}typeReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Arrangement" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AccessRestrictions" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RecordOpeningDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Language" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TypeRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="FormerExternalRef" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="FormerInternalRef" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SecurityTag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Metadata" type="{http://www.tessella.com/XIP/v4}genericMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AccessReviewEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
 *         &lt;element name="DeliverableUnitComponent" type="{http://www.tessella.com/XIP/v4}typeDeliverableUnitComponent" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="BaseVersion" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="CurrentVersion" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;element name="Identifier" type="{http://www.tessella.com/XIP/v4}typeIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Classification" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "typeDeliverableUnit", propOrder = {
    "deliverableUnitRef",
    "collectionRef",
    "recordSeriesRef",
    "accessionRef",
    "accumulationRef",
    "parentRef",
    "digitalSurrogate",
    "catalogueReference",
    "scopeAndContent",
    "closureStatus",
    "closureType",
    "closureCode",
    "coverageFrom",
    "coverageTo",
    "title",
    "creatorCorporateBodyRef",
    "creatorPersonRef",
    "corporateBodyRef",
    "personRef",
    "subjectRef",
    "placeRef",
    "arrangement",
    "accessRestrictions",
    "recordOpeningDate",
    "language",
    "typeRef",
    "formerExternalRef",
    "formerInternalRef",
    "securityTag",
    "metadata",
    "accessReviewEvent",
    "deliverableUnitComponent",
    "baseVersion",
    "currentVersion",
    "identifier",
    "classification"
})
public class TypeDeliverableUnit {

    @XmlElement(name = "DeliverableUnitRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String deliverableUnitRef;
    @XmlElement(name = "CollectionRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String collectionRef;
    @XmlElement(name = "RecordSeriesRef")
    protected Long recordSeriesRef;
    @XmlElement(name = "AccessionRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String accessionRef;
    @XmlElement(name = "AccumulationRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String accumulationRef;
    @XmlElement(name = "ParentRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String parentRef;
    @XmlElement(name = "DigitalSurrogate")
    protected Boolean digitalSurrogate;
    @XmlElement(name = "CatalogueReference", required = true)
    protected String catalogueReference;
    @XmlElement(name = "ScopeAndContent", required = true)
    protected String scopeAndContent;
    @XmlElement(name = "ClosureStatus")
    @XmlSchemaType(name = "NMTOKEN")
    protected TypeClosureStatus closureStatus;
    @XmlElement(name = "ClosureType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String closureType;
    @XmlElement(name = "ClosureCode")
    protected String closureCode;
    @XmlElement(name = "CoverageFrom", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar coverageFrom;
    @XmlElement(name = "CoverageTo", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar coverageTo;
    @XmlElement(name = "Title")
    protected String title;
    @XmlElement(name = "CreatorCorporateBodyRef", type = Long.class)
    protected List<Long> creatorCorporateBodyRef;
    @XmlElement(name = "CreatorPersonRef", type = Long.class)
    protected List<Long> creatorPersonRef;
    @XmlElement(name = "CorporateBodyRef", type = Long.class)
    protected List<Long> corporateBodyRef;
    @XmlElement(name = "PersonRef", type = Long.class)
    protected List<Long> personRef;
    @XmlElement(name = "SubjectRef", type = Long.class)
    protected List<Long> subjectRef;
    @XmlElement(name = "PlaceRef", type = Long.class)
    protected List<Long> placeRef;
    @XmlElement(name = "Arrangement")
    protected String arrangement;
    @XmlElement(name = "AccessRestrictions")
    protected String accessRestrictions;
    @XmlElement(name = "RecordOpeningDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar recordOpeningDate;
    @XmlElement(name = "Language")
    protected String language;
    @XmlElement(name = "TypeRef")
    protected long typeRef;
    @XmlElement(name = "FormerExternalRef")
    protected List<String> formerExternalRef;
    @XmlElement(name = "FormerInternalRef")
    protected List<String> formerInternalRef;
    @XmlElement(name = "SecurityTag", defaultValue = "open")
    protected String securityTag;
    @XmlElement(name = "Metadata")
    protected List<GenericMetadata> metadata;
    @XmlElement(name = "AccessReviewEvent")
    protected TypeEvent accessReviewEvent;
    @XmlElement(name = "DeliverableUnitComponent")
    protected List<TypeDeliverableUnitComponent> deliverableUnitComponent;
    @XmlElement(name = "BaseVersion")
    @XmlSchemaType(name = "anySimpleType")
    protected String baseVersion;
    @XmlElement(name = "CurrentVersion")
    @XmlSchemaType(name = "anySimpleType")
    protected String currentVersion;
    @XmlElement(name = "Identifier")
    protected List<TypeIdentifier> identifier;
    @XmlElement(name = "Classification")
    protected String classification;
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
     * Ruft den Wert der recordSeriesRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRecordSeriesRef() {
        return recordSeriesRef;
    }

    /**
     * Legt den Wert der recordSeriesRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRecordSeriesRef(Long value) {
        this.recordSeriesRef = value;
    }

    /**
     * Ruft den Wert der accessionRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessionRef() {
        return accessionRef;
    }

    /**
     * Legt den Wert der accessionRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessionRef(String value) {
        this.accessionRef = value;
    }

    /**
     * Ruft den Wert der accumulationRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccumulationRef() {
        return accumulationRef;
    }

    /**
     * Legt den Wert der accumulationRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccumulationRef(String value) {
        this.accumulationRef = value;
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
     * Ruft den Wert der digitalSurrogate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDigitalSurrogate() {
        return digitalSurrogate;
    }

    /**
     * Legt den Wert der digitalSurrogate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDigitalSurrogate(Boolean value) {
        this.digitalSurrogate = value;
    }

    /**
     * Ruft den Wert der catalogueReference-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCatalogueReference() {
        return catalogueReference;
    }

    /**
     * Legt den Wert der catalogueReference-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCatalogueReference(String value) {
        this.catalogueReference = value;
    }

    /**
     * Ruft den Wert der scopeAndContent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScopeAndContent() {
        return scopeAndContent;
    }

    /**
     * Legt den Wert der scopeAndContent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScopeAndContent(String value) {
        this.scopeAndContent = value;
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
     * Ruft den Wert der coverageFrom-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCoverageFrom() {
        return coverageFrom;
    }

    /**
     * Legt den Wert der coverageFrom-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCoverageFrom(XMLGregorianCalendar value) {
        this.coverageFrom = value;
    }

    /**
     * Ruft den Wert der coverageTo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCoverageTo() {
        return coverageTo;
    }

    /**
     * Legt den Wert der coverageTo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCoverageTo(XMLGregorianCalendar value) {
        this.coverageTo = value;
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
     * Gets the value of the creatorCorporateBodyRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the creatorCorporateBodyRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCreatorCorporateBodyRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getCreatorCorporateBodyRef() {
        if (creatorCorporateBodyRef == null) {
            creatorCorporateBodyRef = new ArrayList<Long>();
        }
        return this.creatorCorporateBodyRef;
    }

    /**
     * Gets the value of the creatorPersonRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the creatorPersonRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCreatorPersonRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getCreatorPersonRef() {
        if (creatorPersonRef == null) {
            creatorPersonRef = new ArrayList<Long>();
        }
        return this.creatorPersonRef;
    }

    /**
     * Gets the value of the corporateBodyRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the corporateBodyRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCorporateBodyRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getCorporateBodyRef() {
        if (corporateBodyRef == null) {
            corporateBodyRef = new ArrayList<Long>();
        }
        return this.corporateBodyRef;
    }

    /**
     * Gets the value of the personRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the personRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersonRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getPersonRef() {
        if (personRef == null) {
            personRef = new ArrayList<Long>();
        }
        return this.personRef;
    }

    /**
     * Gets the value of the subjectRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subjectRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubjectRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getSubjectRef() {
        if (subjectRef == null) {
            subjectRef = new ArrayList<Long>();
        }
        return this.subjectRef;
    }

    /**
     * Gets the value of the placeRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the placeRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlaceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getPlaceRef() {
        if (placeRef == null) {
            placeRef = new ArrayList<Long>();
        }
        return this.placeRef;
    }

    /**
     * Ruft den Wert der arrangement-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrangement() {
        return arrangement;
    }

    /**
     * Legt den Wert der arrangement-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrangement(String value) {
        this.arrangement = value;
    }

    /**
     * Ruft den Wert der accessRestrictions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessRestrictions() {
        return accessRestrictions;
    }

    /**
     * Legt den Wert der accessRestrictions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessRestrictions(String value) {
        this.accessRestrictions = value;
    }

    /**
     * Ruft den Wert der recordOpeningDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRecordOpeningDate() {
        return recordOpeningDate;
    }

    /**
     * Legt den Wert der recordOpeningDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRecordOpeningDate(XMLGregorianCalendar value) {
        this.recordOpeningDate = value;
    }

    /**
     * Ruft den Wert der language-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Legt den Wert der language-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(String value) {
        this.language = value;
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
     * Gets the value of the formerExternalRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formerExternalRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormerExternalRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFormerExternalRef() {
        if (formerExternalRef == null) {
            formerExternalRef = new ArrayList<String>();
        }
        return this.formerExternalRef;
    }

    /**
     * Gets the value of the formerInternalRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formerInternalRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormerInternalRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFormerInternalRef() {
        if (formerInternalRef == null) {
            formerInternalRef = new ArrayList<String>();
        }
        return this.formerInternalRef;
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
     * Gets the value of the deliverableUnitComponent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deliverableUnitComponent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeliverableUnitComponent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeDeliverableUnitComponent }
     * 
     * 
     */
    public List<TypeDeliverableUnitComponent> getDeliverableUnitComponent() {
        if (deliverableUnitComponent == null) {
            deliverableUnitComponent = new ArrayList<TypeDeliverableUnitComponent>();
        }
        return this.deliverableUnitComponent;
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
     * Ruft den Wert der classification-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassification() {
        return classification;
    }

    /**
     * Legt den Wert der classification-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassification(String value) {
        this.classification = value;
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
