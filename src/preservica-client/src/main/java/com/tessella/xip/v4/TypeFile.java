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
 * A named and ordered sequence of bytes that is known by an operating system.  A file can be zero or more bytes and has a file format, access permissions and file system statistics such as size and last modification date.
 * 
 * <p>Java-Klasse für typeFile complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeFile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FileRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="IngestedFileSetRef" type="{http://www.tessella.com/XIP/v4}typeUnionReference"/>
 *         &lt;element name="FileRelRef" type="{http://www.tessella.com/XIP/v4}typeReference" minOccurs="0"/>
 *         &lt;element name="FileName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Extant" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Directory" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Metadata" type="{http://www.tessella.com/XIP/v4}genericMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="FileSize" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="LastModifiedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="FixityInfo" type="{http://www.tessella.com/XIP/v4}typeFixityInfo" maxOccurs="unbounded"/>
 *         &lt;element name="FormatInfo" type="{http://www.tessella.com/XIP/v4}typeFormatInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="IdentificationStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FormatDiscrepancies" type="{http://www.tessella.com/XIP/v4}arbitraryContents" minOccurs="0"/>
 *         &lt;element name="IdentificationEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" minOccurs="0"/>
 *         &lt;element name="Valid" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Well-formed" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ValidationEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="FileProperty" type="{http://www.tessella.com/XIP/v4}typeFileProperty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PropertyExtractionEvent" type="{http://www.tessella.com/XIP/v4}typeEvent" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="EmbeddedBitstream" type="{http://www.tessella.com/XIP/v4}typeEmbeddedBitstream" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="WorkingPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Identifier" type="{http://www.tessella.com/XIP/v4}typeIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "typeFile", propOrder = {
    "fileRef",
    "ingestedFileSetRef",
    "fileRelRef",
    "fileName",
    "extant",
    "directory",
    "metadata",
    "fileSize",
    "lastModifiedDate",
    "fixityInfo",
    "formatInfo",
    "identificationStatus",
    "formatDiscrepancies",
    "identificationEvent",
    "valid",
    "wellFormed",
    "validationEvent",
    "fileProperty",
    "propertyExtractionEvent",
    "embeddedBitstream",
    "workingPath",
    "identifier",
    "title"
})
public class TypeFile {

    @XmlElement(name = "FileRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String fileRef;
    @XmlElement(name = "IngestedFileSetRef", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String ingestedFileSetRef;
    @XmlElement(name = "FileRelRef")
    protected Long fileRelRef;
    @XmlElement(name = "FileName", required = true)
    protected String fileName;
    @XmlElement(name = "Extant")
    protected boolean extant;
    @XmlElement(name = "Directory")
    protected boolean directory;
    @XmlElement(name = "Metadata")
    protected List<GenericMetadata> metadata;
    @XmlElement(name = "FileSize")
    protected Long fileSize;
    @XmlElement(name = "LastModifiedDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastModifiedDate;
    @XmlElement(name = "FixityInfo", required = true)
    protected List<TypeFixityInfo> fixityInfo;
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
    @XmlElement(name = "EmbeddedBitstream")
    protected List<TypeEmbeddedBitstream> embeddedBitstream;
    @XmlElement(name = "WorkingPath")
    protected String workingPath;
    @XmlElement(name = "Identifier")
    protected List<TypeIdentifier> identifier;
    @XmlElement(name = "Title")
    protected String title;
    @XmlAttribute(name = "status", required = true)
    protected TypeStatus status;

    /**
     * Ruft den Wert der fileRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileRef() {
        return fileRef;
    }

    /**
     * Legt den Wert der fileRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileRef(String value) {
        this.fileRef = value;
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
     * Ruft den Wert der fileRelRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFileRelRef() {
        return fileRelRef;
    }

    /**
     * Legt den Wert der fileRelRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFileRelRef(Long value) {
        this.fileRelRef = value;
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
     * Ruft den Wert der extant-Eigenschaft ab.
     * 
     */
    public boolean isExtant() {
        return extant;
    }

    /**
     * Legt den Wert der extant-Eigenschaft fest.
     * 
     */
    public void setExtant(boolean value) {
        this.extant = value;
    }

    /**
     * Ruft den Wert der directory-Eigenschaft ab.
     * 
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * Legt den Wert der directory-Eigenschaft fest.
     * 
     */
    public void setDirectory(boolean value) {
        this.directory = value;
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
     * Gets the value of the fixityInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixityInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixityInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeFixityInfo }
     * 
     * 
     */
    public List<TypeFixityInfo> getFixityInfo() {
        if (fixityInfo == null) {
            fixityInfo = new ArrayList<TypeFixityInfo>();
        }
        return this.fixityInfo;
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
     * Gets the value of the embeddedBitstream property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the embeddedBitstream property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEmbeddedBitstream().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeEmbeddedBitstream }
     * 
     * 
     */
    public List<TypeEmbeddedBitstream> getEmbeddedBitstream() {
        if (embeddedBitstream == null) {
            embeddedBitstream = new ArrayList<TypeEmbeddedBitstream>();
        }
        return this.embeddedBitstream;
    }

    /**
     * Ruft den Wert der workingPath-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkingPath() {
        return workingPath;
    }

    /**
     * Legt den Wert der workingPath-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkingPath(String value) {
        this.workingPath = value;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the identifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeIdentifier }
     * 
     * 
     */
    public List<TypeIdentifier> getIdentifier() {
        if (identifier == null) {
            identifier = new ArrayList<TypeIdentifier>();
        }
        return this.identifier;
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
