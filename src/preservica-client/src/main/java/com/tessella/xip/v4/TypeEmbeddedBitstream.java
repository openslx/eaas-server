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
 * A contiguous or non-contiguous set of data within a Digital File that has meaningful common properties for preservation purposes.  It cannot be transformed into a standalone file without the addition of file structure (headers, etc.) and/or reformatting the bitstream to comply with some particular file format.
 * 
 * <p>Java-Klasse für typeEmbeddedBitstream complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeEmbeddedBitstream">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EmbeddedBitstreamRelRef" type="{http://www.tessella.com/XIP/v4}typeReference" minOccurs="0"/>
 *         &lt;element name="ParentEmbeddedRelRef" type="{http://www.tessella.com/XIP/v4}typeReference" minOccurs="0"/>
 *         &lt;element name="Type" type="{http://www.tessella.com/XIP/v4}typeBitstreamType" minOccurs="0"/>
 *         &lt;element name="FileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Metadata" type="{http://www.tessella.com/XIP/v4}genericMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="FileSize" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="LastModifiedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="FormatInfo" type="{http://www.tessella.com/XIP/v4}typeFormatInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="IdentificationStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FormatDiscrepancies" type="{http://www.tessella.com/XIP/v4}arbitraryContents" minOccurs="0"/>
 *         &lt;element name="IdentificationEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
 *         &lt;element name="Valid" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Well-formed" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ValidationEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="FileProperty" type="{http://www.tessella.com/XIP/v4}typeFileProperty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PropertyExtractionEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "typeEmbeddedBitstream", propOrder = {
    "embeddedBitstreamRelRef",
    "parentEmbeddedRelRef",
    "type",
    "fileName",
    "metadata",
    "fileSize",
    "lastModifiedDate",
    "formatInfo",
    "identificationStatus",
    "formatDiscrepancies",
    "identificationEvent",
    "valid",
    "wellFormed",
    "validationEvent",
    "fileProperty",
    "propertyExtractionEvent"
})
public class TypeEmbeddedBitstream {

    @XmlElement(name = "EmbeddedBitstreamRelRef")
    protected Long embeddedBitstreamRelRef;
    @XmlElement(name = "ParentEmbeddedRelRef")
    protected Long parentEmbeddedRelRef;
    @XmlElement(name = "Type")
    @XmlSchemaType(name = "string")
    protected TypeBitstreamType type;
    @XmlElement(name = "FileName")
    protected String fileName;
    @XmlElement(name = "Metadata")
    protected List<GenericMetadata> metadata;
    @XmlElement(name = "FileSize")
    protected Long fileSize;
    @XmlElement(name = "LastModifiedDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastModifiedDate;
    @XmlElement(name = "FormatInfo")
    protected List<TypeFormatInfo> formatInfo;
    @XmlElement(name = "IdentificationStatus")
    protected String identificationStatus;
    @XmlElement(name = "FormatDiscrepancies")
    protected ArbitraryContents formatDiscrepancies;
    @XmlElement(name = "IdentificationEvent")
    protected TypeEvent identificationEvent;
    @XmlElement(name = "Valid")
    protected Boolean valid;
    @XmlElement(name = "Well-formed")
    protected Boolean wellFormed;
    @XmlElement(name = "ValidationEvent")
    protected List<TypeEvent> validationEvent;
    @XmlElement(name = "FileProperty")
    protected List<TypeFileProperty> fileProperty;
    @XmlElement(name = "PropertyExtractionEvent")
    protected List<TypeEvent> propertyExtractionEvent;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der embeddedBitstreamRelRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getEmbeddedBitstreamRelRef() {
        return embeddedBitstreamRelRef;
    }

    /**
     * Legt den Wert der embeddedBitstreamRelRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setEmbeddedBitstreamRelRef(Long value) {
        this.embeddedBitstreamRelRef = value;
    }

    /**
     * Ruft den Wert der parentEmbeddedRelRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getParentEmbeddedRelRef() {
        return parentEmbeddedRelRef;
    }

    /**
     * Legt den Wert der parentEmbeddedRelRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setParentEmbeddedRelRef(Long value) {
        this.parentEmbeddedRelRef = value;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeBitstreamType }
     *     
     */
    public TypeBitstreamType getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeBitstreamType }
     *     
     */
    public void setType(TypeBitstreamType value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der fileName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Legt den Wert der fileName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
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
     * Ruft den Wert der fileSize-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Legt den Wert der fileSize-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFileSize(Long value) {
        this.fileSize = value;
    }

    /**
     * Ruft den Wert der lastModifiedDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Legt den Wert der lastModifiedDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastModifiedDate(XMLGregorianCalendar value) {
        this.lastModifiedDate = value;
    }

    /**
     * Gets the value of the formatInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formatInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormatInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeFormatInfo }
     * 
     * 
     */
    public List<TypeFormatInfo> getFormatInfo() {
        if (formatInfo == null) {
            formatInfo = new ArrayList<TypeFormatInfo>();
        }
        return this.formatInfo;
    }

    /**
     * Ruft den Wert der identificationStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificationStatus() {
        return identificationStatus;
    }

    /**
     * Legt den Wert der identificationStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificationStatus(String value) {
        this.identificationStatus = value;
    }

    /**
     * Ruft den Wert der formatDiscrepancies-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArbitraryContents }
     *     
     */
    public ArbitraryContents getFormatDiscrepancies() {
        return formatDiscrepancies;
    }

    /**
     * Legt den Wert der formatDiscrepancies-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArbitraryContents }
     *     
     */
    public void setFormatDiscrepancies(ArbitraryContents value) {
        this.formatDiscrepancies = value;
    }

    /**
     * Ruft den Wert der identificationEvent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeEvent }
     *     
     */
    public TypeEvent getIdentificationEvent() {
        return identificationEvent;
    }

    /**
     * Legt den Wert der identificationEvent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeEvent }
     *     
     */
    public void setIdentificationEvent(TypeEvent value) {
        this.identificationEvent = value;
    }

    /**
     * Ruft den Wert der valid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isValid() {
        return valid;
    }

    /**
     * Legt den Wert der valid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setValid(Boolean value) {
        this.valid = value;
    }

    /**
     * Ruft den Wert der wellFormed-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWellFormed() {
        return wellFormed;
    }

    /**
     * Legt den Wert der wellFormed-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWellFormed(Boolean value) {
        this.wellFormed = value;
    }

    /**
     * Gets the value of the validationEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the validationEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValidationEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeEvent }
     * 
     * 
     */
    public List<TypeEvent> getValidationEvent() {
        if (validationEvent == null) {
            validationEvent = new ArrayList<TypeEvent>();
        }
        return this.validationEvent;
    }

    /**
     * Gets the value of the fileProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fileProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFileProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeFileProperty }
     * 
     * 
     */
    public List<TypeFileProperty> getFileProperty() {
        if (fileProperty == null) {
            fileProperty = new ArrayList<TypeFileProperty>();
        }
        return this.fileProperty;
    }

    /**
     * Gets the value of the propertyExtractionEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyExtractionEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyExtractionEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeEvent }
     * 
     * 
     */
    public List<TypeEvent> getPropertyExtractionEvent() {
        if (propertyExtractionEvent == null) {
            propertyExtractionEvent = new ArrayList<TypeEvent>();
        }
        return this.propertyExtractionEvent;
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
