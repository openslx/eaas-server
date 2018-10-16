//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.05.28 um 08:58:12 PM CEST 
//


package gov.loc.mets;

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
 * areaType: Complex Type for Area Linking
 * 				The area element provides for more sophisticated linking between a div element and content files representing that div, be they text, image, audio, or video files.  An area element can link a div to a point within a file, to a one-dimension segment of a file (e.g., text segment, image line, audio/video clip), or a two-dimensional section of a file 	(e.g, subsection of an image, or a subsection of the  video display of a video file.  The area element has no content; all information is recorded within its various attributes.
 * 			
 * 
 * <p>Java-Klasse für areaType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="areaType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="FILEID" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;attribute name="SHAPE">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="RECT"/>
 *             &lt;enumeration value="CIRCLE"/>
 *             &lt;enumeration value="POLY"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="COORDS" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="BEGIN" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="END" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="BETYPE">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="BYTE"/>
 *             &lt;enumeration value="IDREF"/>
 *             &lt;enumeration value="SMIL"/>
 *             &lt;enumeration value="MIDI"/>
 *             &lt;enumeration value="SMPTE-25"/>
 *             &lt;enumeration value="SMPTE-24"/>
 *             &lt;enumeration value="SMPTE-DF30"/>
 *             &lt;enumeration value="SMPTE-NDF30"/>
 *             &lt;enumeration value="SMPTE-DF29.97"/>
 *             &lt;enumeration value="SMPTE-NDF29.97"/>
 *             &lt;enumeration value="TIME"/>
 *             &lt;enumeration value="TCF"/>
 *             &lt;enumeration value="XPTR"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="EXTENT" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="EXTTYPE">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="BYTE"/>
 *             &lt;enumeration value="SMIL"/>
 *             &lt;enumeration value="MIDI"/>
 *             &lt;enumeration value="SMPTE-25"/>
 *             &lt;enumeration value="SMPTE-24"/>
 *             &lt;enumeration value="SMPTE-DF30"/>
 *             &lt;enumeration value="SMPTE-NDF30"/>
 *             &lt;enumeration value="SMPTE-DF29.97"/>
 *             &lt;enumeration value="SMPTE-NDF29.97"/>
 *             &lt;enumeration value="TIME"/>
 *             &lt;enumeration value="TCF"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *       &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "areaType")
public class AreaType {

    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "FILEID", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object fileid;
    @XmlAttribute(name = "SHAPE")
    protected String shape;
    @XmlAttribute(name = "COORDS")
    protected String coords;
    @XmlAttribute(name = "BEGIN")
    protected String begin;
    @XmlAttribute(name = "END")
    protected String end;
    @XmlAttribute(name = "BETYPE")
    protected String betype;
    @XmlAttribute(name = "EXTENT")
    protected String extent;
    @XmlAttribute(name = "EXTTYPE")
    protected String exttype;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;
    @XmlAttribute(name = "CONTENTIDS")
    protected List<String> contentids;

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
    public Object getFILEID() {
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
    public void setFILEID(Object value) {
        this.fileid = value;
    }

    /**
     * Ruft den Wert der shape-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSHAPE() {
        return shape;
    }

    /**
     * Legt den Wert der shape-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSHAPE(String value) {
        this.shape = value;
    }

    /**
     * Ruft den Wert der coords-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCOORDS() {
        return coords;
    }

    /**
     * Legt den Wert der coords-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCOORDS(String value) {
        this.coords = value;
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
     * Ruft den Wert der extent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEXTENT() {
        return extent;
    }

    /**
     * Legt den Wert der extent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEXTENT(String value) {
        this.extent = value;
    }

    /**
     * Ruft den Wert der exttype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEXTTYPE() {
        return exttype;
    }

    /**
     * Legt den Wert der exttype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEXTTYPE(String value) {
        this.exttype = value;
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
