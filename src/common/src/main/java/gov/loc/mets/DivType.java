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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * divType: Complex Type for Divisions
 * 					The METS standard represents a document structurally as a series of nested div elements, that is, as a hierarchy (e.g., a book, which is composed of chapters, which are composed of subchapters, which are composed of text).  Every div node in the structural map hierarchy may be connected (via subsidiary mptr or fptr elements) to content files which represent that div's portion of the whole document.
 * 
 * SPECIAL NOTE REGARDING DIV ATTRIBUTE VALUES:
 * to clarify the differences between the ORDER, ORDERLABEL, and LABEL attributes for the <div> element, imagine a text with 10 roman numbered pages followed by 10 arabic numbered pages. Page iii would have an ORDER of "3", an ORDERLABEL of "iii" and a LABEL of "Page iii", while page 3 would have an ORDER of "13", an ORDERLABEL of "3" and a LABEL of "Page 3".
 * 			
 * 
 * <p>Java-Klasse für divType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="divType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mptr" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/>
 *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/>
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="fptr" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="par" type="{http://www.loc.gov/METS/}parType" minOccurs="0"/>
 *                   &lt;element name="seq" type="{http://www.loc.gov/METS/}seqType" minOccurs="0"/>
 *                   &lt;element name="area" type="{http://www.loc.gov/METS/}areaType" minOccurs="0"/>
 *                 &lt;/choice>
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="FILEID" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *                 &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="div" type="{http://www.loc.gov/METS/}divType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="ORDER" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="ORDERLABEL" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DMDID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *       &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" />
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}label"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "divType", propOrder = {
    "mptr",
    "fptr",
    "div"
})
public class DivType {

    protected List<DivType.Mptr> mptr;
    protected List<DivType.Fptr> fptr;
    protected List<DivType> div;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "ORDER")
    protected BigInteger order;
    @XmlAttribute(name = "ORDERLABEL")
    protected String orderlabel;
    @XmlAttribute(name = "LABEL")
    protected String label3;
    @XmlAttribute(name = "DMDID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> dmdid;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;
    @XmlAttribute(name = "TYPE")
    protected String type;
    @XmlAttribute(name = "CONTENTIDS")
    protected List<String> contentids;
    @XmlAttribute(name = "label", namespace = "http://www.w3.org/1999/xlink")
    protected String label;

    /**
     * Gets the value of the mptr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mptr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMptr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DivType.Mptr }
     * 
     * 
     */
    public List<DivType.Mptr> getMptr() {
        if (mptr == null) {
            mptr = new ArrayList<DivType.Mptr>();
        }
        return this.mptr;
    }

    /**
     * Gets the value of the fptr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fptr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFptr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DivType.Fptr }
     * 
     * 
     */
    public List<DivType.Fptr> getFptr() {
        if (fptr == null) {
            fptr = new ArrayList<DivType.Fptr>();
        }
        return this.fptr;
    }

    /**
     * Gets the value of the div property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the div property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDiv().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DivType }
     * 
     * 
     */
    public List<DivType> getDiv() {
        if (div == null) {
            div = new ArrayList<DivType>();
        }
        return this.div;
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
     * Ruft den Wert der order-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getORDER() {
        return order;
    }

    /**
     * Legt den Wert der order-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setORDER(BigInteger value) {
        this.order = value;
    }

    /**
     * Ruft den Wert der orderlabel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getORDERLABEL() {
        return orderlabel;
    }

    /**
     * Legt den Wert der orderlabel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setORDERLABEL(String value) {
        this.orderlabel = value;
    }

    /**
     * Ruft den Wert der label3-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel3() {
        return label3;
    }

    /**
     * Legt den Wert der label3-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel3(String value) {
        this.label3 = value;
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
     * Gets the value of the contentids property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contentids property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCONTENTIDS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCONTENTIDS() {
        if (contentids == null) {
            contentids = new ArrayList<String>();
        }
        return this.contentids;
    }

    /**
     * xlink:label - an xlink label to be referred to by an smLink element
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Legt den Wert der label-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
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
     *         &lt;element name="par" type="{http://www.loc.gov/METS/}parType" minOccurs="0"/>
     *         &lt;element name="seq" type="{http://www.loc.gov/METS/}seqType" minOccurs="0"/>
     *         &lt;element name="area" type="{http://www.loc.gov/METS/}areaType" minOccurs="0"/>
     *       &lt;/choice>
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="FILEID" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
     *       &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "par",
        "seq",
        "area"
    })
    public static class Fptr {

        protected ParType par;
        protected SeqType seq;
        protected AreaType area;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "FILEID")
        protected String fileid;
        @XmlAttribute(name = "CONTENTIDS")
        protected List<String> contentids;

        /**
         * Ruft den Wert der par-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ParType }
         *     
         */
        public ParType getPar() {
            return par;
        }

        /**
         * Legt den Wert der par-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ParType }
         *     
         */
        public void setPar(ParType value) {
            this.par = value;
        }

        /**
         * Ruft den Wert der seq-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link SeqType }
         *     
         */
        public SeqType getSeq() {
            return seq;
        }

        /**
         * Legt den Wert der seq-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link SeqType }
         *     
         */
        public void setSeq(SeqType value) {
            this.seq = value;
        }

        /**
         * Ruft den Wert der area-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link AreaType }
         *     
         */
        public AreaType getArea() {
            return area;
        }

        /**
         * Legt den Wert der area-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link AreaType }
         *     
         */
        public void setArea(AreaType value) {
            this.area = value;
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
         * Ruft den Wert der fileid-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public String getFILEID() {
            return fileid;
        }

        /**
         * Legt den Wert der fileid-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setFILEID(String value) {
            this.fileid = value;
        }

        /**
         * Gets the value of the contentids property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the contentids property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCONTENTIDS().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getCONTENTIDS() {
            if (contentids == null) {
                contentids = new ArrayList<String>();
            }
            return this.contentids;
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
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/>
     *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/>
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Mptr {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "CONTENTIDS")
        protected List<String> contentids;
        @XmlAttribute(name = "LOCTYPE", required = true)
        protected String loctype;
        @XmlAttribute(name = "OTHERLOCTYPE")
        protected String otherloctype;
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
         * Gets the value of the contentids property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the contentids property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCONTENTIDS().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getCONTENTIDS() {
            if (contentids == null) {
                contentids = new ArrayList<String>();
            }
            return this.contentids;
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

    }

}
