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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für recordType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="recordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="leader" type="{http://www.loc.gov/MARC21/slim}leaderFieldType"/>
 *         &lt;element name="controlfield" type="{http://www.loc.gov/MARC21/slim}controlFieldType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="datafield" type="{http://www.loc.gov/MARC21/slim}dataFieldType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://www.loc.gov/MARC21/slim}recordTypeType" />
 *       &lt;attribute name="id" type="{http://www.loc.gov/MARC21/slim}idDataType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "recordType", propOrder = {
    "leader",
    "controlfield",
    "datafield"
})
public class RecordType {

    protected LeaderFieldType leader;
    protected List<ControlFieldType> controlfield;
    protected List<DataFieldType> datafield;
    @XmlAttribute(name = "type")
    protected RecordTypeType type;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Ruft den Wert der leader-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LeaderFieldType }
     *     
     */
    public LeaderFieldType getLeader() {
        return leader;
    }

    /**
     * Legt den Wert der leader-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LeaderFieldType }
     *     
     */
    public void setLeader(LeaderFieldType value) {
        this.leader = value;
    }

    /**
     * Gets the value of the controlfield property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the controlfield property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getControlfield().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ControlFieldType }
     * 
     * 
     */
    public List<ControlFieldType> getControlfield() {
        if (controlfield == null) {
            controlfield = new ArrayList<ControlFieldType>();
        }
        return this.controlfield;
    }

    /**
     * Gets the value of the datafield property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the datafield property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatafield().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataFieldType }
     * 
     * 
     */
    public List<DataFieldType> getDatafield() {
        if (datafield == null) {
            datafield = new ArrayList<DataFieldType>();
        }
        return this.datafield;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RecordTypeType }
     *     
     */
    public RecordTypeType getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RecordTypeType }
     *     
     */
    public void setType(RecordTypeType value) {
        this.type = value;
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

}
