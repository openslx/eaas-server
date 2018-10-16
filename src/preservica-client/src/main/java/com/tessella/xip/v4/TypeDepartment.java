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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * A collection containing material only from a particular source (governmental or organisational) department. Deprecated.
 * 
 * <p>Java-Klasse für typeDepartment complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeDepartment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DepartmentRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="DepartmentCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ClosureStatus" type="{http://www.tessella.com/XIP/v4}typeClosureStatus"/>
 *         &lt;element name="ClosureType" type="{http://www.tessella.com/XIP/v4}typeClosureType"/>
 *         &lt;element name="ClosureCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "typeDepartment", propOrder = {
    "departmentRef",
    "departmentCode",
    "title",
    "closureStatus",
    "closureType",
    "closureCode"
})
public class TypeDepartment {

    @XmlElement(name = "DepartmentRef")
    protected long departmentRef;
    @XmlElement(name = "DepartmentCode", required = true)
    protected String departmentCode;
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
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

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
     * Ruft den Wert der departmentCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDepartmentCode() {
        return departmentCode;
    }

    /**
     * Legt den Wert der departmentCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepartmentCode(String value) {
        this.departmentCode = value;
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
