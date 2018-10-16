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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * A part of an Accumulation that is delivered in a single transaction.  It consists of a group of Preservation Objects (both physical, i.e. files, and conceptual, i.e. Deliverable Units).
 * 
 * <p>Java-Klasse für typeAccession complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeAccession">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AccessionRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="AccumulationRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="IngestedFileSetRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Metadata" type="{http://www.tessella.com/XIP/v4}genericMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AccessionEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
 *         &lt;element name="SystemMigrationEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="TransferEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
 *         &lt;element name="RedactionEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
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
@XmlType(name = "typeAccession", propOrder = {
    "accessionRef",
    "accumulationRef",
    "ingestedFileSetRef",
    "date",
    "metadata",
    "accessionEvent",
    "systemMigrationEvent",
    "transferEvent",
    "redactionEvent"
})
public class TypeAccession {

    @XmlElement(name = "AccessionRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String accessionRef;
    @XmlElement(name = "AccumulationRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String accumulationRef;
    @XmlElement(name = "IngestedFileSetRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String ingestedFileSetRef;
    @XmlElement(name = "Date", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar date;
    @XmlElement(name = "Metadata")
    protected List<GenericMetadata> metadata;
    @XmlElement(name = "AccessionEvent")
    protected TypeEvent accessionEvent;
    @XmlElement(name = "SystemMigrationEvent")
    protected List<TypeEvent> systemMigrationEvent;
    @XmlElement(name = "TransferEvent")
    protected TypeEvent transferEvent;
    @XmlElement(name = "RedactionEvent")
    protected TypeEvent redactionEvent;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

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
     * Ruft den Wert der ingestedFileSetRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIngestedFileSetRef() {
        return ingestedFileSetRef;
    }

    /**
     * Legt den Wert der ingestedFileSetRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIngestedFileSetRef(String value) {
        this.ingestedFileSetRef = value;
    }

    /**
     * Ruft den Wert der date-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDate() {
        return date;
    }

    /**
     * Legt den Wert der date-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
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
     * Ruft den Wert der accessionEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getAccessionEvent() {
        return accessionEvent;
    }

    /**
     * Legt den Wert der accessionEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setAccessionEvent(TypeEvent value) {
        this.accessionEvent = value;
    }

    /**
     * Gets the value of the systemMigrationEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the systemMigrationEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSystemMigrationEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeEvent }
     * 
     * 
     */
    public List<TypeEvent> getSystemMigrationEvent() {
        if (systemMigrationEvent == null) {
            systemMigrationEvent = new ArrayList<TypeEvent>();
        }
        return this.systemMigrationEvent;
    }

    /**
     * Ruft den Wert der transferEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getTransferEvent() {
        return transferEvent;
    }

    /**
     * Legt den Wert der transferEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setTransferEvent(TypeEvent value) {
        this.transferEvent = value;
    }

    /**
     * Ruft den Wert der redactionEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getRedactionEvent() {
        return redactionEvent;
    }

    /**
     * Legt den Wert der redactionEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setRedactionEvent(TypeEvent value) {
        this.redactionEvent = value;
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
