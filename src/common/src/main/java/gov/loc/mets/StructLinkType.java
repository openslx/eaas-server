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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * structLinkType: Complex Type for Structural Map Linking
 * 				The Structural Map Linking section allows for the specification of hyperlinks between different components of a METS structure delineated in a structural map.  structLink contains a single, repeatable element, smLink.  Each smLink element indicates a hyperlink between two nodes in the structMap.  The structMap nodes recorded in smLink are identified using their XML ID attribute	values.
 * 			
 * 
 * <p>Java-Klasse für structLinkType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="structLinkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="smLink">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}arcrole"/>
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}title"/>
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}show"/>
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}actuate"/>
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}to use="required""/>
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}from use="required""/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="smLinkGrp">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="smLocatorLink" maxOccurs="unbounded" minOccurs="2">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attGroup ref="{http://www.w3.org/1999/xlink}locatorLink"/>
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="smArcLink" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attGroup ref="{http://www.w3.org/1999/xlink}arcLink"/>
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                           &lt;attribute name="ARCTYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}extendedLink"/>
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="ARCLINKORDER" default="unordered">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="ordered"/>
 *                       &lt;enumeration value="unordered"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/choice>
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "structLinkType", propOrder = {
    "smLinkOrSmLinkGrp"
})
@XmlSeeAlso({
    gov.loc.mets.MetsType.StructLink.class
})
public class StructLinkType {

    @XmlElements({
        @XmlElement(name = "smLink", type = StructLinkType.SmLink.class),
        @XmlElement(name = "smLinkGrp", type = StructLinkType.SmLinkGrp.class)
    })
    protected List<Object> smLinkOrSmLinkGrp;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the smLinkOrSmLinkGrp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the smLinkOrSmLinkGrp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSmLinkOrSmLinkGrp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StructLinkType.SmLink }
     * {@link StructLinkType.SmLinkGrp }
     * 
     * 
     */
    public List<Object> getSmLinkOrSmLinkGrp() {
        if (smLinkOrSmLinkGrp == null) {
            smLinkOrSmLinkGrp = new ArrayList<Object>();
        }
        return this.smLinkOrSmLinkGrp;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}arcrole"/>
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}title"/>
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}show"/>
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}actuate"/>
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}to use="required""/>
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}from use="required""/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SmLink {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
        protected String arcrole;
        @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
        protected String title;
        @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
        protected String show;
        @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
        protected String actuate;
        @XmlAttribute(name = "to", namespace = "http://www.w3.org/1999/xlink", required = true)
        protected String to;
        @XmlAttribute(name = "from", namespace = "http://www.w3.org/1999/xlink", required = true)
        protected String from;

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
         * 
         * 								 xlink:arcrole - the role of the link, as per the xlink specification.  See http://www.w3.org/TR/xlink/
         * 							
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
         * 
         * 								xlink:title - a title for the link (if needed), as per the xlink specification.  See http://www.w3.org/TR/xlink/
         * 							
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
         * 
         * 								xlink:show - see the xlink specification at http://www.w3.org/TR/xlink/
         * 							
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
         * 
         * 								xlink:actuate - see the xlink specification at http://www.w3.org/TR/xlink/
         * 							
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
         * 
         * 								xlink:to - the value of the label for the element in the structMap you are linking to.
         * 							
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTo() {
            return to;
        }

        /**
         * Legt den Wert der to-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTo(String value) {
            this.to = value;
        }

        /**
         * 
         * 								xlink:from - the value of the label for the element in the structMap you are linking from.
         * 							
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFrom() {
            return from;
        }

        /**
         * Legt den Wert der from-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFrom(String value) {
            this.from = value;
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
     *         &lt;element name="smLocatorLink" maxOccurs="unbounded" minOccurs="2">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}locatorLink"/>
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="smArcLink" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}arcLink"/>
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *                 &lt;attribute name="ARCTYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}extendedLink"/>
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="ARCLINKORDER" default="unordered">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;enumeration value="ordered"/>
     *             &lt;enumeration value="unordered"/>
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
    @XmlType(name = "", propOrder = {
        "smLocatorLink",
        "smArcLink"
    })
    public static class SmLinkGrp {

        @XmlElement(required = true)
        protected List<StructLinkType.SmLinkGrp.SmLocatorLink> smLocatorLink;
        @XmlElement(required = true)
        protected List<StructLinkType.SmLinkGrp.SmArcLink> smArcLink;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "ARCLINKORDER")
        protected String arclinkorder;
        @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
        protected String type;
        @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
        protected String role;
        @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
        protected String title;

        /**
         * Gets the value of the smLocatorLink property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the smLocatorLink property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSmLocatorLink().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link StructLinkType.SmLinkGrp.SmLocatorLink }
         * 
         * 
         */
        public List<StructLinkType.SmLinkGrp.SmLocatorLink> getSmLocatorLink() {
            if (smLocatorLink == null) {
                smLocatorLink = new ArrayList<StructLinkType.SmLinkGrp.SmLocatorLink>();
            }
            return this.smLocatorLink;
        }

        /**
         * Gets the value of the smArcLink property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the smArcLink property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSmArcLink().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link StructLinkType.SmLinkGrp.SmArcLink }
         * 
         * 
         */
        public List<StructLinkType.SmLinkGrp.SmArcLink> getSmArcLink() {
            if (smArcLink == null) {
                smArcLink = new ArrayList<StructLinkType.SmLinkGrp.SmArcLink>();
            }
            return this.smArcLink;
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
         * Ruft den Wert der arclinkorder-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getARCLINKORDER() {
            if (arclinkorder == null) {
                return "unordered";
            } else {
                return arclinkorder;
            }
        }

        /**
         * Legt den Wert der arclinkorder-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setARCLINKORDER(String value) {
            this.arclinkorder = value;
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
                return "extended";
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
         * 
         * 										The structMap arc link element <smArcLink> is of xlink:type "arc" It can be used to establish a traversal link between two <div> elements as identified by <smLocatorLink> elements within the same smLinkGrp element. The associated xlink:from and xlink:to attributes identify the from and to sides of the arc link by referencing the xlink:label attribute values on the participating smLocatorLink elements.
         * 									
         * 
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}arcLink"/>
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
         *       &lt;attribute name="ARCTYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SmArcLink {

            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "ARCTYPE")
            protected String arctype;
            @XmlAttribute(name = "ADMID")
            @XmlIDREF
            @XmlSchemaType(name = "IDREFS")
            protected List<Object> admid;
            @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
            protected String type;
            @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
            protected String arcrole;
            @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
            protected String title;
            @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
            protected String show;
            @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
            protected String actuate;
            @XmlAttribute(name = "from", namespace = "http://www.w3.org/1999/xlink")
            protected String from;
            @XmlAttribute(name = "to", namespace = "http://www.w3.org/1999/xlink")
            protected String to;

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
             * Ruft den Wert der arctype-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getARCTYPE() {
                return arctype;
            }

            /**
             * Legt den Wert der arctype-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setARCTYPE(String value) {
                this.arctype = value;
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
            public String getType() {
                if (type == null) {
                    return "arc";
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
             * Ruft den Wert der from-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFrom() {
                return from;
            }

            /**
             * Legt den Wert der from-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFrom(String value) {
                this.from = value;
            }

            /**
             * Ruft den Wert der to-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTo() {
                return to;
            }

            /**
             * Legt den Wert der to-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTo(String value) {
                this.to = value;
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
         *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}locatorLink"/>
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SmLocatorLink {

            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
            protected String type;
            @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink", required = true)
            @XmlSchemaType(name = "anyURI")
            protected String href;
            @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
            protected String role;
            @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
            protected String title;
            @XmlAttribute(name = "label", namespace = "http://www.w3.org/1999/xlink")
            protected String label;

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
            public String getType() {
                if (type == null) {
                    return "locator";
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
             * Ruft den Wert der label-Eigenschaft ab.
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

        }

    }

}
