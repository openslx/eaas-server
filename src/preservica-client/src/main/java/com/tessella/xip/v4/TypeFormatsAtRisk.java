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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Used for recording formats at risk of obsolescence and therefore in need of preservation action.  The file formats included are those where:
 * - one of the file format inherent properties is flagged as high risk 
 * - one of the file format instance properties is flagged as high risk 
 * - the sum of the risk scores for the inherent properties associated with the file format is greater than a supplied threshold risk value.
 * 
 * <p>Java-Klasse für typeFormatsAtRisk complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeFormatsAtRisk">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MigrationPathwayType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Format" type="{http://www.tessella.com/XIP/v4}typeFormat" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeFormatsAtRisk", propOrder = {
    "migrationPathwayType",
    "format"
})
public class TypeFormatsAtRisk {

    @XmlElement(name = "MigrationPathwayType", required = true)
    protected String migrationPathwayType;
    @XmlElement(name = "Format")
    protected List<TypeFormat> format;

    /**
     * Ruft den Wert der migrationPathwayType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMigrationPathwayType() {
        return migrationPathwayType;
    }

    /**
     * Legt den Wert der migrationPathwayType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMigrationPathwayType(String value) {
        this.migrationPathwayType = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the format property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeFormat }
     * 
     * 
     */
    public List<TypeFormat> getFormat() {
        if (format == null) {
            format = new ArrayList<TypeFormat>();
        }
        return this.format;
    }

}
