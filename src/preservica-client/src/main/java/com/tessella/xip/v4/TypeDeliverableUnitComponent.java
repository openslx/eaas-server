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
import javax.xml.bind.annotation.XmlType;


/**
 * A part, or the whole of, a Deliverable Unit about which invariant properties can be measured.  It should be preserved in any migration.
 * 
 * <p>Java-Klasse für typeDeliverableUnitComponent complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeDeliverableUnitComponent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ComponentRef" type="{http://www.tessella.com/XIP/v4}typeGUID"/>
 *         &lt;element name="ComponentType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ComponentCreationEvent" type="{http://www.tessella.com/XIP/v4}typeEvent"/>
 *         &lt;element name="ComponentProperty" type="{http://www.tessella.com/XIP/v4}typeComponentProperty" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "typeDeliverableUnitComponent", propOrder = {
    "componentRef",
    "componentType",
    "description",
    "componentCreationEvent",
    "componentProperty"
})
public class TypeDeliverableUnitComponent {

    @XmlElement(name = "ComponentRef", required = true)
    protected String componentRef;
    @XmlElement(name = "ComponentType", required = true)
    protected String componentType;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "ComponentCreationEvent", required = true)
    protected TypeEvent componentCreationEvent;
    @XmlElement(name = "ComponentProperty")
    protected List<TypeComponentProperty> componentProperty;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der componentRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentRef() {
        return componentRef;
    }

    /**
     * Legt den Wert der componentRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentRef(String value) {
        this.componentRef = value;
    }

    /**
     * Ruft den Wert der componentType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentType() {
        return componentType;
    }

    /**
     * Legt den Wert der componentType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentType(String value) {
        this.componentType = value;
    }

    /**
     * Ruft den Wert der description-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der componentCreationEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getComponentCreationEvent() {
        return componentCreationEvent;
    }

    /**
     * Legt den Wert der componentCreationEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setComponentCreationEvent(TypeEvent value) {
        this.componentCreationEvent = value;
    }

    /**
     * Gets the value of the componentProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the componentProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComponentProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeComponentProperty }
     * 
     * 
     */
    public List<TypeComponentProperty> getComponentProperty() {
        if (componentProperty == null) {
            componentProperty = new ArrayList<TypeComponentProperty>();
        }
        return this.componentProperty;
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
