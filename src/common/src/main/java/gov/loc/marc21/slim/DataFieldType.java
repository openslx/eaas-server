//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.11.18 um 08:02:10 PM CET 
//


package gov.loc.marc21.slim;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * MARC21 Variable Data Fields 010-999
 * 
 * <p>Java-Klasse für dataFieldType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="dataFieldType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element name="subfield" type="{http://www.loc.gov/MARC21/slim}subfieldatafieldType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.loc.gov/MARC21/slim}idDataType" />
 *       &lt;attribute name="tag" use="required" type="{http://www.loc.gov/MARC21/slim}tagDataType" />
 *       &lt;attribute name="ind1" use="required" type="{http://www.loc.gov/MARC21/slim}indicatorDataType" />
 *       &lt;attribute name="ind2" use="required" type="{http://www.loc.gov/MARC21/slim}indicatorDataType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataFieldType", propOrder = {
    "subfield"
})
public class DataFieldType {

    @XmlElement(required = true)
    protected List<SubfieldatafieldType> subfield;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(name = "tag", required = true)
    protected String tag;
    @XmlAttribute(name = "ind1", required = true)
    protected String ind1;
    @XmlAttribute(name = "ind2", required = true)
    protected String ind2;

    /**
     * Gets the value of the subfield property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subfield property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubfield().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubfieldatafieldType }
     * 
     * 
     */
    public List<SubfieldatafieldType> getSubfield() {
        if (subfield == null) {
            subfield = new ArrayList<SubfieldatafieldType>();
        }
        return this.subfield;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
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
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der tag-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTag() {
        return tag;
    }

    /**
     * Legt den Wert der tag-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTag(String value) {
        this.tag = value;
    }

    /**
     * Ruft den Wert der ind1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInd1() {
        return ind1;
    }

    /**
     * Legt den Wert der ind1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInd1(String value) {
        this.ind1 = value;
    }

    /**
     * Ruft den Wert der ind2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInd2() {
        return ind2;
    }

    /**
     * Legt den Wert der ind2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInd2(String value) {
        this.ind2 = value;
    }

}
