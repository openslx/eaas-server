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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * behaviorType: Complex Type for Behaviors
 * 			 A behavior can be used to associate executable behaviors with content in the METS object.  A behavior element has an interface definition element that represents an abstract definition  of the set  of behaviors represented by a particular behavior.  A behavior element also has an behavior  mechanism which is a module of executable code that implements and runs the behavior defined abstractly by the interface definition.
 * 			
 * 
 * <p>Java-Klasse für behaviorType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="behaviorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="interfaceDef" type="{http://www.loc.gov/METS/}objectType" minOccurs="0"/>
 *         &lt;element name="mechanism" type="{http://www.loc.gov/METS/}objectType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="STRUCTID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *       &lt;attribute name="BTYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="CREATED" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="GROUPID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "behaviorType", propOrder = {
    "interfaceDef",
    "mechanism"
})
public class BehaviorType {

    protected ObjectType interfaceDef;
    @XmlElement(required = true)
    protected ObjectType mechanism;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "STRUCTID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> structid;
    @XmlAttribute(name = "BTYPE")
    protected String btype;
    @XmlAttribute(name = "CREATED")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar created;
    @XmlAttribute(name = "LABEL")
    protected String label5;
    @XmlAttribute(name = "GROUPID")
    protected String groupid;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;

    /**
     * Ruft den Wert der interfaceDef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ObjectType }
     *     
     */
    public ObjectType getInterfaceDef() {
        return interfaceDef;
    }

    /**
     * Legt den Wert der interfaceDef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectType }
     *     
     */
    public void setInterfaceDef(ObjectType value) {
        this.interfaceDef = value;
    }

    /**
     * Ruft den Wert der mechanism-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ObjectType }
     *     
     */
    public ObjectType getMechanism() {
        return mechanism;
    }

    /**
     * Legt den Wert der mechanism-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectType }
     *     
     */
    public void setMechanism(ObjectType value) {
        this.mechanism = value;
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
     * Gets the value of the structid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the structid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSTRUCTID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getSTRUCTID() {
        if (structid == null) {
            structid = new ArrayList<Object>();
        }
        return this.structid;
    }

    /**
     * Ruft den Wert der btype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBTYPE() {
        return btype;
    }

    /**
     * Legt den Wert der btype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBTYPE(String value) {
        this.btype = value;
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
     * Ruft den Wert der label5-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel5() {
        return label5;
    }

    /**
     * Legt den Wert der label5-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel5(String value) {
        this.label5 = value;
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

}
