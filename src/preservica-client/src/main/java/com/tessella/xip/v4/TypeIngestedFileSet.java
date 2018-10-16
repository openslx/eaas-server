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
 * A set of files that are deposited into the archive together as a result of particular event (e.g. an accession event or a transformation event).
 * 
 * <p>Java-Klasse für typeIngestedFileSet complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeIngestedFileSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IngestedFileSetRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="IngestedFileSetTypeRef" type="{http://www.tessella.com/XIP/v4}typeReference"/>
 *         &lt;element name="VirusCheckEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="IntegrityCheckEventRef" type="{http://www.tessella.com/XIP/v4}typeReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="TransformationUnitRef" type="{http://www.tessella.com/XIP/v4}typeGUID" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="AccessionRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" minOccurs="0"/>
 *         &lt;/choice>
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
@XmlType(name = "typeIngestedFileSet", propOrder = {
    "ingestedFileSetRef",
    "ingestedFileSetTypeRef",
    "virusCheckEvent",
    "integrityCheckEventRef",
    "transformationUnitRef",
    "accessionRef"
})
public class TypeIngestedFileSet {

    @XmlElement(name = "IngestedFileSetRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String ingestedFileSetRef;
    @XmlElement(name = "IngestedFileSetTypeRef")
    protected long ingestedFileSetTypeRef;
    @XmlElement(name = "VirusCheckEvent")
    protected List<TypeEvent> virusCheckEvent;
    @XmlElement(name = "IntegrityCheckEventRef", type = Long.class)
    protected List<Long> integrityCheckEventRef;
    @XmlElement(name = "TransformationUnitRef")
    protected List<String> transformationUnitRef;
    @XmlElement(name = "AccessionRef")
    @XmlSchemaType(name = "anySimpleType")
    protected String accessionRef;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

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
     * Ruft den Wert der ingestedFileSetTypeRef-Eigenschaft ab.
     * 
     */
    public long getIngestedFileSetTypeRef() {
        return ingestedFileSetTypeRef;
    }

    /**
     * Legt den Wert der ingestedFileSetTypeRef-Eigenschaft fest.
     * 
     */
    public void setIngestedFileSetTypeRef(long value) {
        this.ingestedFileSetTypeRef = value;
    }

    /**
     * Gets the value of the virusCheckEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the virusCheckEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVirusCheckEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeEvent }
     * 
     * 
     */
    public List<TypeEvent> getVirusCheckEvent() {
        if (virusCheckEvent == null) {
            virusCheckEvent = new ArrayList<TypeEvent>();
        }
        return this.virusCheckEvent;
    }

    /**
     * Gets the value of the integrityCheckEventRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the integrityCheckEventRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIntegrityCheckEventRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getIntegrityCheckEventRef() {
        if (integrityCheckEventRef == null) {
            integrityCheckEventRef = new ArrayList<Long>();
        }
        return this.integrityCheckEventRef;
    }

    /**
     * Gets the value of the transformationUnitRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transformationUnitRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransformationUnitRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getTransformationUnitRef() {
        if (transformationUnitRef == null) {
            transformationUnitRef = new ArrayList<String>();
        }
        return this.transformationUnitRef;
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
