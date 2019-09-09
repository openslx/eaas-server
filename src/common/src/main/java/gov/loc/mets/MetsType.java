//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.05.28 um 08:58:12 PM CEST 
//


package gov.loc.mets;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * metsType: Complex Type for METS Sections
 * 			A METS document consists of seven possible subsidiary sections: metsHdr (METS document header), dmdSec (descriptive metadata section), amdSec (administrative metadata section), fileGrp (file inventory group), structLink (structural map linking), structMap (structural map) and behaviorSec (behaviors section).
 * 			
 * 
 * <p>Java-Klasse für metsType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="metsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metsHdr" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="agent" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                           &lt;attribute name="ROLE" use="required">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;enumeration value="CREATOR"/>
 *                                 &lt;enumeration value="EDITOR"/>
 *                                 &lt;enumeration value="ARCHIVIST"/>
 *                                 &lt;enumeration value="PRESERVATION"/>
 *                                 &lt;enumeration value="DISSEMINATOR"/>
 *                                 &lt;enumeration value="CUSTODIAN"/>
 *                                 &lt;enumeration value="IPOWNER"/>
 *                                 &lt;enumeration value="OTHER"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="OTHERROLE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="TYPE">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;enumeration value="INDIVIDUAL"/>
 *                                 &lt;enumeration value="ORGANIZATION"/>
 *                                 &lt;enumeration value="OTHER"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="OTHERTYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="altRecordID" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                           &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="metsDocumentID" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                           &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *                 &lt;attribute name="CREATEDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="LASTMODDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="RECORDSTATUS" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="dmdSec" type="{http://www.loc.gov/METS/}mdSecType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="amdSec" type="{http://www.loc.gov/METS/}amdSecType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="fileSec" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="fileGrp" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://www.loc.gov/METS/}fileGrpType">
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="structMap" type="{http://www.loc.gov/METS/}structMapType" maxOccurs="unbounded"/>
 *         &lt;element name="structLink" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.loc.gov/METS/}structLinkType">
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="behaviorSec" type="{http://www.loc.gov/METS/}behaviorSecType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="OBJID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="PROFILE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "metsType", propOrder = {
    "metsHdr",
    "dmdSec",
    "amdSec",
    "fileSec",
    "structMap",
    "structLink",
    "behaviorSec"
})
@XmlSeeAlso({
    Mets.class
})
public class MetsType extends JaxbType {

    protected MetsType.MetsHdr metsHdr;
    protected List<MdSecType> dmdSec;
    protected List<AmdSecType> amdSec;
    protected MetsType.FileSec fileSec;
    // @XmlElement(required = true)
    protected List<StructMapType> structMap;
    protected MetsType.StructLink structLink;
    protected List<BehaviorSecType> behaviorSec;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "OBJID")
    protected String objid;
    @XmlAttribute(name = "LABEL")
    protected String label1;
    @XmlAttribute(name = "TYPE")
    protected String type;
    @XmlAttribute(name = "PROFILE")
    protected String profile;

    /**
     * Ruft den Wert der metsHdr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MetsType.MetsHdr }
     *     
     */
    public MetsType.MetsHdr getMetsHdr() {
        return metsHdr;
    }

    /**
     * Legt den Wert der metsHdr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MetsType.MetsHdr }
     *     
     */
    public void setMetsHdr(MetsType.MetsHdr value) {
        this.metsHdr = value;
    }

    /**
     * Gets the value of the dmdSec property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dmdSec property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDmdSec().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MdSecType }
     * 
     * 
     */
    public List<MdSecType> getDmdSec() {
        if (dmdSec == null) {
            dmdSec = new ArrayList<MdSecType>();
        }
        return this.dmdSec;
    }

    /**
     * Gets the value of the amdSec property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the amdSec property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAmdSec().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AmdSecType }
     * 
     * 
     */
    public List<AmdSecType> getAmdSec() {
        if (amdSec == null) {
            amdSec = new ArrayList<AmdSecType>();
        }
        return this.amdSec;
    }

    /**
     * Ruft den Wert der fileSec-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MetsType.FileSec }
     *     
     */
    public MetsType.FileSec getFileSec() {
        return fileSec;
    }

    /**
     * Legt den Wert der fileSec-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MetsType.FileSec }
     *     
     */
    public void setFileSec(MetsType.FileSec value) {
        this.fileSec = value;
    }

    /**
     * Gets the value of the structMap property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the structMap property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStructMap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StructMapType }
     * 
     * 
     */
    public List<StructMapType> getStructMap() {
        if (structMap == null) {
            structMap = new ArrayList<StructMapType>();
        }
        return this.structMap;
    }

    /**
     * Ruft den Wert der structLink-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MetsType.StructLink }
     *     
     */
    public MetsType.StructLink getStructLink() {
        return structLink;
    }

    /**
     * Legt den Wert der structLink-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MetsType.StructLink }
     *     
     */
    public void setStructLink(MetsType.StructLink value) {
        this.structLink = value;
    }

    /**
     * Gets the value of the behaviorSec property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the behaviorSec property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBehaviorSec().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BehaviorSecType }
     * 
     * 
     */
    public List<BehaviorSecType> getBehaviorSec() {
        if (behaviorSec == null) {
            behaviorSec = new ArrayList<BehaviorSecType>();
        }
        return this.behaviorSec;
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
     * Ruft den Wert der objid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOBJID() {
        return objid;
    }

    /**
     * Legt den Wert der objid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOBJID(String value) {
        this.objid = value;
    }

    /**
     * Ruft den Wert der label1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel1() {
        return label1;
    }

    /**
     * Legt den Wert der label1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel1(String value) {
        this.label1 = value;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTYPE() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTYPE(String value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der profile-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPROFILE() {
        return profile;
    }

    /**
     * Legt den Wert der profile-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPROFILE(String value) {
        this.profile = value;
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
     *         &lt;element name="fileGrp" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://www.loc.gov/METS/}fileGrpType">
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fileGrp"
    })
    public static class FileSec {

        @XmlElement(required = true)
        protected List<MetsType.FileSec.FileGrp> fileGrp;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;

        /**
         * Gets the value of the fileGrp property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the fileGrp property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFileGrp().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MetsType.FileSec.FileGrp }
         * 
         * 
         */
        public List<MetsType.FileSec.FileGrp> getFileGrp() {
            if (fileGrp == null) {
                fileGrp = new ArrayList<MetsType.FileSec.FileGrp>();
            }
            return this.fileGrp;
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
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://www.loc.gov/METS/}fileGrpType">
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class FileGrp
            extends FileGrpType
        {


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
     *       &lt;sequence>
     *         &lt;element name="agent" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *                 &lt;attribute name="ROLE" use="required">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       &lt;enumeration value="CREATOR"/>
     *                       &lt;enumeration value="EDITOR"/>
     *                       &lt;enumeration value="ARCHIVIST"/>
     *                       &lt;enumeration value="PRESERVATION"/>
     *                       &lt;enumeration value="DISSEMINATOR"/>
     *                       &lt;enumeration value="CUSTODIAN"/>
     *                       &lt;enumeration value="IPOWNER"/>
     *                       &lt;enumeration value="OTHER"/>
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *                 &lt;attribute name="OTHERROLE" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="TYPE">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       &lt;enumeration value="INDIVIDUAL"/>
     *                       &lt;enumeration value="ORGANIZATION"/>
     *                       &lt;enumeration value="OTHER"/>
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *                 &lt;attribute name="OTHERTYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="altRecordID" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;simpleContent>
     *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *                 &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/extension>
     *             &lt;/simpleContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="metsDocumentID" minOccurs="0">
     *           &lt;complexType>
     *             &lt;simpleContent>
     *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *                 &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/extension>
     *             &lt;/simpleContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
     *       &lt;attribute name="CREATEDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="LASTMODDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="RECORDSTATUS" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "agent",
        "altRecordID",
        "metsDocumentID"
    })
    public static class MetsHdr {

        protected List<MetsType.MetsHdr.Agent> agent;
        protected List<MetsType.MetsHdr.AltRecordID> altRecordID;
        protected MetsType.MetsHdr.MetsDocumentID metsDocumentID;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "ADMID")
        @XmlIDREF
        @XmlSchemaType(name = "IDREFS")
        protected List<Object> admid;
        @XmlAttribute(name = "CREATEDATE")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar createdate;
        @XmlAttribute(name = "LASTMODDATE")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar lastmoddate;
        @XmlAttribute(name = "RECORDSTATUS")
        protected String recordstatus;

        /**
         * Gets the value of the agent property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the agent property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAgent().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MetsType.MetsHdr.Agent }
         * 
         * 
         */
        public List<MetsType.MetsHdr.Agent> getAgent() {
            if (agent == null) {
                agent = new ArrayList<MetsType.MetsHdr.Agent>();
            }
            return this.agent;
        }

        /**
         * Gets the value of the altRecordID property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the altRecordID property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAltRecordID().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MetsType.MetsHdr.AltRecordID }
         * 
         * 
         */
        public List<MetsType.MetsHdr.AltRecordID> getAltRecordID() {
            if (altRecordID == null) {
                altRecordID = new ArrayList<MetsType.MetsHdr.AltRecordID>();
            }
            return this.altRecordID;
        }

        /**
         * Ruft den Wert der metsDocumentID-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link MetsType.MetsHdr.MetsDocumentID }
         *     
         */
        public MetsType.MetsHdr.MetsDocumentID getMetsDocumentID() {
            return metsDocumentID;
        }

        /**
         * Legt den Wert der metsDocumentID-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link MetsType.MetsHdr.MetsDocumentID }
         *     
         */
        public void setMetsDocumentID(MetsType.MetsHdr.MetsDocumentID value) {
            this.metsDocumentID = value;
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
         * Ruft den Wert der createdate-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getCREATEDATE() {
            return createdate;
        }

        /**
         * Legt den Wert der createdate-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setCREATEDATE(XMLGregorianCalendar value) {
            this.createdate = value;
        }

        /**
         * Ruft den Wert der lastmoddate-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getLASTMODDATE() {
            return lastmoddate;
        }

        /**
         * Legt den Wert der lastmoddate-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setLASTMODDATE(XMLGregorianCalendar value) {
            this.lastmoddate = value;
        }

        /**
         * Ruft den Wert der recordstatus-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRECORDSTATUS() {
            return recordstatus;
        }

        /**
         * Legt den Wert der recordstatus-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRECORDSTATUS(String value) {
            this.recordstatus = value;
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
         *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
         *       &lt;/sequence>
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
         *       &lt;attribute name="ROLE" use="required">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             &lt;enumeration value="CREATOR"/>
         *             &lt;enumeration value="EDITOR"/>
         *             &lt;enumeration value="ARCHIVIST"/>
         *             &lt;enumeration value="PRESERVATION"/>
         *             &lt;enumeration value="DISSEMINATOR"/>
         *             &lt;enumeration value="CUSTODIAN"/>
         *             &lt;enumeration value="IPOWNER"/>
         *             &lt;enumeration value="OTHER"/>
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *       &lt;attribute name="OTHERROLE" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="TYPE">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             &lt;enumeration value="INDIVIDUAL"/>
         *             &lt;enumeration value="ORGANIZATION"/>
         *             &lt;enumeration value="OTHER"/>
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *       &lt;attribute name="OTHERTYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "name",
            "note"
        })
        public static class Agent {

            @XmlElement(required = true)
            protected String name;
            protected List<String> note;
            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "ROLE", required = true)
            protected String role;
            @XmlAttribute(name = "OTHERROLE")
            protected String otherrole;
            @XmlAttribute(name = "TYPE")
            protected String type;
            @XmlAttribute(name = "OTHERTYPE")
            protected String othertype;

            /**
             * Ruft den Wert der name-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Legt den Wert der name-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the note property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the note property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getNote().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            public List<String> getNote() {
                if (note == null) {
                    note = new ArrayList<String>();
                }
                return this.note;
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
             * Ruft den Wert der role-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getROLE() {
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
            public void setROLE(String value) {
                this.role = value;
            }

            /**
             * Ruft den Wert der otherrole-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getOTHERROLE() {
                return otherrole;
            }

            /**
             * Legt den Wert der otherrole-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOTHERROLE(String value) {
                this.otherrole = value;
            }

            /**
             * Ruft den Wert der type-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTYPE() {
                return type;
            }

            /**
             * Legt den Wert der type-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTYPE(String value) {
                this.type = value;
            }

            /**
             * Ruft den Wert der othertype-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getOTHERTYPE() {
                return othertype;
            }

            /**
             * Legt den Wert der othertype-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOTHERTYPE(String value) {
                this.othertype = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;simpleContent>
         *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
         *       &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/extension>
         *   &lt;/simpleContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class AltRecordID {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "TYPE")
            protected String type;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
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
             * Ruft den Wert der type-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTYPE() {
                return type;
            }

            /**
             * Legt den Wert der type-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTYPE(String value) {
                this.type = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;simpleContent>
         *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
         *       &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/extension>
         *   &lt;/simpleContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class MetsDocumentID {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "TYPE")
            protected String type;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
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
             * Ruft den Wert der type-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTYPE() {
                return type;
            }

            /**
             * Legt den Wert der type-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTYPE(String value) {
                this.type = value;
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
     *     &lt;extension base="{http://www.loc.gov/METS/}structLinkType">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StructLink
        extends StructLinkType
    {


    }

}
