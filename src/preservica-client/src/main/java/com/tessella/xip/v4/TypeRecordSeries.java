//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.16 um 03:18:14 PM CET 
//


package com.tessella.xip.v4;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * A Record Series is a logical grouping of Accumulations by a specific Department for a specific business reason.  For example, a record series could be: Original correspondence and papers classified under the headings: civil, military, private and secret, and miscellaneous; and correspondence and papers relating to special subjects: Catholic emancipation, 1824; peerage claims, 1828 to 1831; poor law reform, 1836 to 1839; reports of outrages, 1836 to 1840; and the report of the Irish Land Commission, 1820 to 1823. Deprecated.
 * 
 * <p>Java-Klasse für typeRecordSeries complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeRecordSeries">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RecordSeriesRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="DepartmentRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="RecordSeriesCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ClosureStatus" type="{http://www.tessella.com/XIP/v4}typeClosureStatus"/>
 *         &lt;element name="ClosureType" type="{http://www.tessella.com/XIP/v4}typeClosureType"/>
 *         &lt;element name="ClosureCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ClassNumber" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="SubclassNumber" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="AccessReviewEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
 *         &lt;element name="CustodialHistory" type="{http://www.tessella.com/XIP/v4}arbitraryContents" minOccurs="0"/>
 *         &lt;element name="PreservationHistory" type="{http://www.tessella.com/XIP/v4}arbitraryContents" minOccurs="0"/>
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
@XmlType(name = "typeRecordSeries", propOrder = {
    "recordSeriesRef",
    "departmentRef",
    "recordSeriesCode",
    "title",
    "closureStatus",
    "closureType",
    "closureCode",
    "classNumber",
    "subclassNumber",
    "accessReviewEvent",
    "custodialHistory",
    "preservationHistory"
})
public class TypeRecordSeries {

    @XmlElement(name = "RecordSeriesRef")
    protected long recordSeriesRef;
    @XmlElement(name = "DepartmentRef")
    protected long departmentRef;
    @XmlElement(name = "RecordSeriesCode")
    protected String recordSeriesCode;
    @XmlElement(name = "Title", required = true)
    protected String title;
    @XmlElement(name = "ClosureStatus", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected TypeClosureStatus closureStatus;
    @XmlElement(name = "ClosureType", required = true)
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
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der recordSeriesRef-Eigenschaft ab.
     * 
     */
    public long getRecordSeriesRef() {
        return recordSeriesRef;
    }

    /**
     * Legt den Wert der recordSeriesRef-Eigenschaft fest.
     * 
     */
    public void setRecordSeriesRef(long value) {
        this.recordSeriesRef = value;
    }

    /**
     * Ruft den Wert der departmentRef-Eigenschaft ab.
     * 
     */
    public long getDepartmentRef() {
        return departmentRef;
    }

    /**
     * Legt den Wert der departmentRef-Eigenschaft fest.
     * 
     */
    public void setDepartmentRef(long value) {
        this.departmentRef = value;
    }

    /**
     * Ruft den Wert der recordSeriesCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordSeriesCode() {
        return recordSeriesCode;
    }

    /**
     * Legt den Wert der recordSeriesCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordSeriesCode(String value) {
        this.recordSeriesCode = value;
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
