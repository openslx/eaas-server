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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * A record of a significant action that occurred in the lifetime of one or more objects known to SDB.  Note that no information about the objects involved is held with the event entity; instead the objects reference the event, which enables a single event entity to be shared by all the objects that participated in it.
 * 
 * <p>Java-Klasse für typeEvent complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeEvent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EventRef" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="EventTypeRef" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="EventDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="EventAgent" type="{http://www.tessella.com/XIP/v4}arbitraryContents" minOccurs="0"/>
 *         &lt;element name="Process" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Outcome" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeEvent", propOrder = {
    "eventRef",
    "eventTypeRef",
    "eventDate",
    "eventAgent",
    "process",
    "outcome"
})
public class TypeEvent {

    @XmlElement(name = "EventRef")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger eventRef;
    @XmlElement(name = "EventTypeRef")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger eventTypeRef;
    @XmlElement(name = "EventDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar eventDate;
    @XmlElement(name = "EventAgent")
    protected ArbitraryContents eventAgent;
    @XmlElement(name = "Process")
    protected String process;
    @XmlElement(name = "Outcome")
    protected String outcome;

    /**
     * Ruft den Wert der eventRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getEventRef() {
        return eventRef;
    }

    /**
     * Legt den Wert der eventRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setEventRef(BigInteger value) {
        this.eventRef = value;
    }

    /**
     * Ruft den Wert der eventTypeRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getEventTypeRef() {
        return eventTypeRef;
    }

    /**
     * Legt den Wert der eventTypeRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setEventTypeRef(BigInteger value) {
        this.eventTypeRef = value;
    }

    /**
     * Ruft den Wert der eventDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEventDate() {
        return eventDate;
    }

    /**
     * Legt den Wert der eventDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEventDate(XMLGregorianCalendar value) {
        this.eventDate = value;
    }

    /**
     * Ruft den Wert der eventAgent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArbitraryContents }
     *     
     */
    public ArbitraryContents getEventAgent() {
        return eventAgent;
    }

    /**
     * Legt den Wert der eventAgent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArbitraryContents }
     *     
     */
    public void setEventAgent(ArbitraryContents value) {
        this.eventAgent = value;
    }

    /**
     * Ruft den Wert der process-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcess() {
        return process;
    }

    /**
     * Legt den Wert der process-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcess(String value) {
        this.process = value;
    }

    /**
     * Ruft den Wert der outcome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutcome() {
        return outcome;
    }

    /**
     * Legt den Wert der outcome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutcome(String value) {
        this.outcome = value;
    }

}
