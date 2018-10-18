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
 * An instance of a particular Component in a particular Manifestation of a Deliverable Unit.
 * 
 * <p>Java-Klasse für typeComponentManifestation complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeComponentManifestation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ComponentRef" type="{http://www.tessella.com/XIP/v4}typeGUID"/>
 *         &lt;element name="ComponentManifestationRef" type="{http://www.tessella.com/XIP/v4}typeGUID"/>
 *         &lt;element name="LinkedComponentManifestationRef" type="{http://www.tessella.com/XIP/v4}typeGUID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ComponentType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ComponentManifestationType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MasterFileRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="FileRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Measurement" type="{http://www.tessella.com/XIP/v4}typeComponentManifestationMeasurement" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "typeComponentManifestation", propOrder = {
    "componentRef",
    "componentManifestationRef",
    "linkedComponentManifestationRef",
    "componentType",
    "componentManifestationType",
    "masterFileRef",
    "fileRef",
    "measurement"
})
public class TypeComponentManifestation {

    @XmlElement(name = "ComponentRef", required = true)
    protected String componentRef;
    @XmlElement(name = "ComponentManifestationRef", required = true)
    protected String componentManifestationRef;
    @XmlElement(name = "LinkedComponentManifestationRef")
    protected List<String> linkedComponentManifestationRef;
    @XmlElement(name = "ComponentType", required = true)
    protected String componentType;
    @XmlElement(name = "ComponentManifestationType", required = true)
    protected String componentManifestationType;
    @XmlElement(name = "MasterFileRef")
    @XmlSchemaType(name = "anySimpleType")
    protected List<String> masterFileRef;
    @XmlElement(name = "FileRef")
    @XmlSchemaType(name = "anySimpleType")
    protected List<String> fileRef;
    @XmlElement(name = "Measurement")
    protected List<TypeComponentManifestationMeasurement> measurement;
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
     * Ruft den Wert der componentManifestationRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentManifestationRef() {
        return componentManifestationRef;
    }

    /**
     * Legt den Wert der componentManifestationRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentManifestationRef(String value) {
        this.componentManifestationRef = value;
    }

    /**
     * Gets the value of the linkedComponentManifestationRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkedComponentManifestationRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkedComponentManifestationRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkedComponentManifestationRef() {
        if (linkedComponentManifestationRef == null) {
            linkedComponentManifestationRef = new ArrayList<String>();
        }
        return this.linkedComponentManifestationRef;
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
     * Ruft den Wert der componentManifestationType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentManifestationType() {
        return componentManifestationType;
    }

    /**
     * Legt den Wert der componentManifestationType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentManifestationType(String value) {
        this.componentManifestationType = value;
    }

    /**
     * Gets the value of the masterFileRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the masterFileRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMasterFileRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMasterFileRef() {
        if (masterFileRef == null) {
            masterFileRef = new ArrayList<String>();
        }
        return this.masterFileRef;
    }

    /**
     * Gets the value of the fileRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fileRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFileRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFileRef() {
        if (fileRef == null) {
            fileRef = new ArrayList<String>();
        }
        return this.fileRef;
    }

    /**
     * Gets the value of the measurement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the measurement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeasurement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeComponentManifestationMeasurement }
     * 
     * 
     */
    public List<TypeComponentManifestationMeasurement> getMeasurement() {
        if (measurement == null) {
            measurement = new ArrayList<TypeComponentManifestationMeasurement>();
        }
        return this.measurement;
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
