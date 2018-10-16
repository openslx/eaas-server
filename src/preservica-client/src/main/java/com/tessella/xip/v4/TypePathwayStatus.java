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
 * Details of whether the Migration Pathway has been approved for use and by whom and on what date.
 * 
 * <p>Java-Klasse für typePathwayStatus complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typePathwayStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ApprovedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ApprovedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="approved" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typePathwayStatus", propOrder = {
    "approvedBy",
    "approvedDate"
})
public class TypePathwayStatus {

    @XmlElement(name = "ApprovedBy")
    protected String approvedBy;
    @XmlElement(name = "ApprovedDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar approvedDate;
    @XmlAttribute(name = "approved")
    protected Boolean approved;

    /**
     * Ruft den Wert der approvedBy-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApprovedBy() {
        return approvedBy;
    }

    /**
     * Legt den Wert der approvedBy-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApprovedBy(String value) {
        this.approvedBy = value;
    }

    /**
     * Ruft den Wert der approvedDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getApprovedDate() {
        return approvedDate;
    }

    /**
     * Legt den Wert der approvedDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setApprovedDate(XMLGregorianCalendar value) {
        this.approvedDate = value;
    }

    /**
     * Ruft den Wert der approved-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isApproved() {
        if (approved == null) {
            return false;
        } else {
            return approved;
        }
    }

    /**
     * Legt den Wert der approved-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApproved(Boolean value) {
        this.approved = value;
    }

}
