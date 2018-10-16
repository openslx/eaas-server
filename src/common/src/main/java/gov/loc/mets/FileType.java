//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.05.28 um 08:58:12 PM CEST 
//


package gov.loc.mets;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import org.w3c.dom.Element;


/**
 * fileType: Complex Type for Files
 * 				The file element provides access to content files for a METS object.  A file element may contain one or more FLocat elements, which provide pointers to a content file, and/or an FContent element, which wraps an encoded version of the file. Note that ALL FLocat and FContent elements underneath a single file element should identify/contain identical copies of a single file.
 * 			
 * 
 * <p>Java-Klasse für fileType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fileType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FLocat" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/>
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/>
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="FContent" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="binData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *                   &lt;element name="xmlData" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;any processContents='lax' maxOccurs="unbounded"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/choice>
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="stream" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="streamType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="OWNERID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *                 &lt;attribute name="DMDID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *                 &lt;attribute name="BEGIN" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="END" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="BETYPE">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="BYTE"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="transformFile" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="TRANSFORMTYPE" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="decompression"/>
 *                       &lt;enumeration value="decryption"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="TRANSFORMALGORITHM" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="TRANSFORMKEY" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="TRANSFORMBEHAVIOR" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *                 &lt;attribute name="TRANSFORMORDER" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="file" type="{http://www.loc.gov/METS/}fileType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.loc.gov/METS/}FILECORE"/>
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="SEQ" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="OWNERID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *       &lt;attribute name="DMDID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *       &lt;attribute name="GROUPID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="BEGIN" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="END" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="BETYPE">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="BYTE"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileType", propOrder = {
    "fLocat",
    "fContent",
    "stream",
    "transformFile",
    "file"
})
public class FileType {

    @XmlElement(name = "FLocat")
    protected List<FileType.FLocat> fLocat;
    @XmlElement(name = "FContent")
    protected FileType.FContent fContent;
    protected List<FileType.Stream> stream;
    protected List<FileType.TransformFile> transformFile;
    protected List<FileType> file;
    @XmlAttribute(name = "ID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "SEQ")
    protected Integer seq;
    @XmlAttribute(name = "OWNERID")
    protected String ownerid;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;
    @XmlAttribute(name = "DMDID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> dmdid;
    @XmlAttribute(name = "GROUPID")
    protected String groupid;
    @XmlAttribute(name = "USE")
    protected String use;
    @XmlAttribute(name = "BEGIN")
    protected String begin;
    @XmlAttribute(name = "END")
    protected String end;
    @XmlAttribute(name = "BETYPE")
    protected String betype;
    @XmlAttribute(name = "MIMETYPE")
    protected String mimetype;
    @XmlAttribute(name = "SIZE")
    protected Long size;
    @XmlAttribute(name = "CREATED")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar created;
    @XmlAttribute(name = "CHECKSUM")
    protected String checksum;
    @XmlAttribute(name = "CHECKSUMTYPE")
    protected String checksumtype;

    /**
     * Gets the value of the fLocat property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fLocat property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFLocat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileType.FLocat }
     * 
     * 
     */
    public List<FileType.FLocat> getFLocat() {
        if (fLocat == null) {
            fLocat = new ArrayList<FileType.FLocat>();
        }
        return this.fLocat;
    }

    /**
     * Ruft den Wert der fContent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FileType.FContent }
     *     
     */
    public FileType.FContent getFContent() {
        return fContent;
    }

    /**
     * Legt den Wert der fContent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FileType.FContent }
     *     
     */
    public void setFContent(FileType.FContent value) {
        this.fContent = value;
    }

    /**
     * Gets the value of the stream property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stream property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStream().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileType.Stream }
     * 
     * 
     */
    public List<FileType.Stream> getStream() {
        if (stream == null) {
            stream = new ArrayList<FileType.Stream>();
        }
        return this.stream;
    }

    /**
     * Gets the value of the transformFile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transformFile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransformFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileType.TransformFile }
     * 
     * 
     */
    public List<FileType.TransformFile> getTransformFile() {
        if (transformFile == null) {
            transformFile = new ArrayList<FileType.TransformFile>();
        }
        return this.transformFile;
    }

    /**
     * Gets the value of the file property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the file property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileType }
     * 
     * 
     */
    public List<FileType> getFile() {
        if (file == null) {
            file = new ArrayList<FileType>();
        }
        return this.file;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der seq-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSEQ() {
        return seq;
    }

    /**
     * Legt den Wert der seq-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSEQ(Integer value) {
        this.seq = value;
    }

    /**
     * Ruft den Wert der ownerid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOWNERID() {
        return ownerid;
    }

    /**
     * Legt den Wert der ownerid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOWNERID(String value) {
        this.ownerid = value;
    }

    /**
     * Gets the value of the admid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the admid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getADMID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getADMID() {
        if (admid == null) {
            admid = new ArrayList<Object>();
        }
        return this.admid;
    }

    /**
     * Gets the value of the dmdid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dmdid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDMDID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getDMDID() {
        if (dmdid == null) {
            dmdid = new ArrayList<Object>();
        }
        return this.dmdid;
    }

    /**
     * Ruft den Wert der groupid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGROUPID() {
        return groupid;
    }

    /**
     * Legt den Wert der groupid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGROUPID(String value) {
        this.groupid = value;
    }

    /**
     * Ruft den Wert der use-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUSE() {
        return use;
    }

    /**
     * Legt den Wert der use-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUSE(String value) {
        this.use = value;
    }

    /**
     * Ruft den Wert der begin-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBEGIN() {
        return begin;
    }

    /**
     * Legt den Wert der begin-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBEGIN(String value) {
        this.begin = value;
    }

    /**
     * Ruft den Wert der end-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEND() {
        return end;
    }

    /**
     * Legt den Wert der end-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEND(String value) {
        this.end = value;
    }

    /**
     * Ruft den Wert der betype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBETYPE() {
        return betype;
    }

    /**
     * Legt den Wert der betype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBETYPE(String value) {
        this.betype = value;
    }

    /**
     * Ruft den Wert der mimetype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMIMETYPE() {
        return mimetype;
    }

    /**
     * Legt den Wert der mimetype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMIMETYPE(String value) {
        this.mimetype = value;
    }

    /**
     * Ruft den Wert der size-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSIZE() {
        return size;
    }

    /**
     * Legt den Wert der size-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSIZE(Long value) {
        this.size = value;
    }

    /**
     * Ruft den Wert der created-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCREATED() {
        return created;
    }

    /**
     * Legt den Wert der created-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCREATED(XMLGregorianCalendar value) {
        this.created = value;
    }

    /**
     * Ruft den Wert der checksum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCHECKSUM() {
        return checksum;
    }

    /**
     * Legt den Wert der checksum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCHECKSUM(String value) {
        this.checksum = value;
    }

    /**
     * Ruft den Wert der checksumtype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCHECKSUMTYPE() {
        return checksumtype;
    }

    /**
     * Legt den Wert der checksumtype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCHECKSUMTYPE(String value) {
        this.checksumtype = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;element name="binData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
     *         &lt;element name="xmlData" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;any processContents='lax' maxOccurs="unbounded"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/choice>
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "binData",
        "xmlData"
    })
    public static class FContent {

        protected byte[] binData;
        protected FileType.FContent.XmlData xmlData;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "USE")
        protected String use;

        /**
         * Ruft den Wert der binData-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getBinData() {
            return binData;
        }

        /**
         * Legt den Wert der binData-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setBinData(byte[] value) {
            this.binData = value;
        }

        /**
         * Ruft den Wert der xmlData-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link FileType.FContent.XmlData }
         *     
         */
        public FileType.FContent.XmlData getXmlData() {
            return xmlData;
        }

        /**
         * Legt den Wert der xmlData-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link FileType.FContent.XmlData }
         *     
         */
        public void setXmlData(FileType.FContent.XmlData value) {
            this.xmlData = value;
        }

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Ruft den Wert der use-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUSE() {
            return use;
        }

        /**
         * Legt den Wert der use-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUSE(String value) {
            this.use = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;any processContents='lax' maxOccurs="unbounded"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "any"
        })
        public static class XmlData {

            @XmlAnyElement(lax = true)
            protected List<Object> any;

            /**
             * Gets the value of the any property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the any property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAny().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Object }
             * {@link Element }
             * 
             * 
             */
            public List<Object> getAny() {
                if (any == null) {
                    any = new ArrayList<Object>();
                }
                return this.any;
            }

        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/>
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/>
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class FLocat {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "USE")
        protected String use;
        @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
        protected String type;
        @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
        @XmlSchemaType(name = "anyURI")
        protected String href;
        @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
        protected String role;
        @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
        protected String arcrole;
        @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
        protected String title;
        @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
        protected String show;
        @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
        protected String actuate;
        @XmlAttribute(name = "LOCTYPE", required = true)
        protected String loctype;
        @XmlAttribute(name = "OTHERLOCTYPE")
        protected String otherloctype;

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Ruft den Wert der use-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUSE() {
            return use;
        }

        /**
         * Legt den Wert der use-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUSE(String value) {
            this.use = value;
        }

        /**
         * Ruft den Wert der type-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            if (type == null) {
                return "simple";
            } else {
                return type;
            }
        }

        /**
         * Legt den Wert der type-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Ruft den Wert der href-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getHref() {
            return href;
        }

        /**
         * Legt den Wert der href-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHref(String value) {
            this.href = value;
        }

        /**
         * Ruft den Wert der role-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRole() {
            return role;
        }

        /**
         * Legt den Wert der role-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRole(String value) {
            this.role = value;
        }

        /**
         * Ruft den Wert der arcrole-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getArcrole() {
            return arcrole;
        }

        /**
         * Legt den Wert der arcrole-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setArcrole(String value) {
            this.arcrole = value;
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
         * Ruft den Wert der show-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getShow() {
            return show;
        }

        /**
         * Legt den Wert der show-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setShow(String value) {
            this.show = value;
        }

        /**
         * Ruft den Wert der actuate-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getActuate() {
            return actuate;
        }

        /**
         * Legt den Wert der actuate-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setActuate(String value) {
            this.actuate = value;
        }

        /**
         * Ruft den Wert der loctype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLOCTYPE() {
            return loctype;
        }

        /**
         * Legt den Wert der loctype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLOCTYPE(String value) {
            this.loctype = value;
        }

        /**
         * Ruft den Wert der otherloctype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOTHERLOCTYPE() {
            return otherloctype;
        }

        /**
         * Legt den Wert der otherloctype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOTHERLOCTYPE(String value) {
            this.otherloctype = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="streamType" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="OWNERID" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
     *       &lt;attribute name="DMDID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
     *       &lt;attribute name="BEGIN" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="END" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="BETYPE">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;enumeration value="BYTE"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Stream {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "streamType")
        protected String streamType;
        @XmlAttribute(name = "OWNERID")
        protected String ownerid;
        @XmlAttribute(name = "ADMID")
        @XmlIDREF
        @XmlSchemaType(name = "IDREFS")
        protected List<Object> admid;
        @XmlAttribute(name = "DMDID")
        @XmlIDREF
        @XmlSchemaType(name = "IDREFS")
        protected List<Object> dmdid;
        @XmlAttribute(name = "BEGIN")
        protected String begin;
        @XmlAttribute(name = "END")
        protected String end;
        @XmlAttribute(name = "BETYPE")
        protected String betype;

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Ruft den Wert der streamType-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getStreamType() {
            return streamType;
        }

        /**
         * Legt den Wert der streamType-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStreamType(String value) {
            this.streamType = value;
        }

        /**
         * Ruft den Wert der ownerid-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOWNERID() {
            return ownerid;
        }

        /**
         * Legt den Wert der ownerid-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOWNERID(String value) {
            this.ownerid = value;
        }

        /**
         * Gets the value of the admid property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the admid property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getADMID().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        public List<Object> getADMID() {
            if (admid == null) {
                admid = new ArrayList<Object>();
            }
            return this.admid;
        }

        /**
         * Gets the value of the dmdid property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the dmdid property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDMDID().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        public List<Object> getDMDID() {
            if (dmdid == null) {
                dmdid = new ArrayList<Object>();
            }
            return this.dmdid;
        }

        /**
         * Ruft den Wert der begin-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getBEGIN() {
            return begin;
        }

        /**
         * Legt den Wert der begin-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBEGIN(String value) {
            this.begin = value;
        }

        /**
         * Ruft den Wert der end-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEND() {
            return end;
        }

        /**
         * Legt den Wert der end-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEND(String value) {
            this.end = value;
        }

        /**
         * Ruft den Wert der betype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getBETYPE() {
            return betype;
        }

        /**
         * Legt den Wert der betype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBETYPE(String value) {
            this.betype = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="TRANSFORMTYPE" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;enumeration value="decompression"/>
     *             &lt;enumeration value="decryption"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *       &lt;attribute name="TRANSFORMALGORITHM" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="TRANSFORMKEY" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="TRANSFORMBEHAVIOR" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
     *       &lt;attribute name="TRANSFORMORDER" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class TransformFile {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "TRANSFORMTYPE", required = true)
        protected String transformtype;
        @XmlAttribute(name = "TRANSFORMALGORITHM", required = true)
        protected String transformalgorithm;
        @XmlAttribute(name = "TRANSFORMKEY")
        protected String transformkey;
        @XmlAttribute(name = "TRANSFORMBEHAVIOR")
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object transformbehavior;
        @XmlAttribute(name = "TRANSFORMORDER", required = true)
        @XmlSchemaType(name = "positiveInteger")
        protected BigInteger transformorder;

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Ruft den Wert der transformtype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTRANSFORMTYPE() {
            return transformtype;
        }

        /**
         * Legt den Wert der transformtype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTRANSFORMTYPE(String value) {
            this.transformtype = value;
        }

        /**
         * Ruft den Wert der transformalgorithm-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTRANSFORMALGORITHM() {
            return transformalgorithm;
        }

        /**
         * Legt den Wert der transformalgorithm-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTRANSFORMALGORITHM(String value) {
            this.transformalgorithm = value;
        }

        /**
         * Ruft den Wert der transformkey-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTRANSFORMKEY() {
            return transformkey;
        }

        /**
         * Legt den Wert der transformkey-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTRANSFORMKEY(String value) {
            this.transformkey = value;
        }

        /**
         * Ruft den Wert der transformbehavior-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getTRANSFORMBEHAVIOR() {
            return transformbehavior;
        }

        /**
         * Legt den Wert der transformbehavior-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setTRANSFORMBEHAVIOR(Object value) {
            this.transformbehavior = value;
        }

        /**
         * Ruft den Wert der transformorder-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getTRANSFORMORDER() {
            return transformorder;
        }

        /**
         * Legt den Wert der transformorder-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setTRANSFORMORDER(BigInteger value) {
            this.transformorder = value;
        }

    }

}
