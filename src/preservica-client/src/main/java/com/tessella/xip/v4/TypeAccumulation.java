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
 * A series of (one or more) related transfers of Information Objects from a single entity (person, organisation or part of an organisation).
 * 
 * <p>Java-Klasse für typeAccumulation complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeAccumulation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AccumulationRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;choice>
 *           &lt;element name="CollectionRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *           &lt;element name="RecordSeriesRef" type="{http://www.tessella.com/XIP/v4}typeReference" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="Metadata" type="{http://www.tessella.com/XIP/v4}genericMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SelectionEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
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
@XmlType(name = "typeAccumulation", propOrder = {
    "accumulationRef",
    "collectionRef",
    "recordSeriesRef",
    "metadata",
    "selectionEvent"
})
public class TypeAccumulation {

    @XmlElement(name = "AccumulationRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String accumulationRef;
    @XmlElement(name = "CollectionRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String collectionRef;
    @XmlElement(name = "RecordSeriesRef")
    protected Long recordSeriesRef;
    @XmlElement(name = "Metadata")
    protected List<GenericMetadata> metadata;
    @XmlElement(name = "SelectionEvent")
    protected TypeEvent selectionEvent;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

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
     * Ruft den Wert der selectionEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getSelectionEvent() {
        return selectionEvent;
    }

    /**
     * Legt den Wert der selectionEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setSelectionEvent(TypeEvent value) {
        this.selectionEvent = value;
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
