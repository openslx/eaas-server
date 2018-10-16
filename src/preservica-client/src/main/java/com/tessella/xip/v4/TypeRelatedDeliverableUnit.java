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


/**
 * A link between two Deliverable Units that have a direct and significant connection (from the point of view of the "From Deliverable Unit").
 * 
 * <p>Java-Klasse für typeRelatedDeliverableUnit complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeRelatedDeliverableUnit">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FromDeliverableUnitRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="ToDeliverableUnitRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="Relationship" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "typeRelatedDeliverableUnit", propOrder = {
    "fromDeliverableUnitRef",
    "toDeliverableUnitRef",
    "relationship"
})
public class TypeRelatedDeliverableUnit {

    @XmlElement(name = "FromDeliverableUnitRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String fromDeliverableUnitRef;
    @XmlElement(name = "ToDeliverableUnitRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String toDeliverableUnitRef;
    @XmlElement(name = "Relationship", required = true)
    protected String relationship;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der fromDeliverableUnitRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFromDeliverableUnitRef() {
        return fromDeliverableUnitRef;
    }

    /**
     * Legt den Wert der fromDeliverableUnitRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromDeliverableUnitRef(String value) {
        this.fromDeliverableUnitRef = value;
    }

    /**
     * Ruft den Wert der toDeliverableUnitRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToDeliverableUnitRef() {
        return toDeliverableUnitRef;
    }

    /**
     * Legt den Wert der toDeliverableUnitRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToDeliverableUnitRef(String value) {
        this.toDeliverableUnitRef = value;
    }

    /**
     * Ruft den Wert der relationship-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelationship() {
        return relationship;
    }

    /**
     * Legt den Wert der relationship-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelationship(String value) {
        this.relationship = value;
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
