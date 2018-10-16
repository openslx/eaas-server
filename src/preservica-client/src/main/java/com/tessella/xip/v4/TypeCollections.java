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
 * <p>Java-Klasse für typeCollections complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeCollections">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="Collection" type="{http://www.tessella.com/XIP/v4}typeCollection" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="DepartmentsAndSeries" type="{http://www.tessella.com/XIP/v4}typeDepartmentsAndSeries" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeCollections", propOrder = {
    "collection",
    "departmentsAndSeries"
})
public class TypeCollections {

    @XmlElement(name = "Collection")
    protected List<TypeCollection> collection;
    @XmlElement(name = "DepartmentsAndSeries")
    protected TypeDepartmentsAndSeries departmentsAndSeries;

    /**
     * Gets the value of the collection property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the collection property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCollection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeCollection }
     * 
     * 
     */
    public List<TypeCollection> getCollection() {
        if (collection == null) {
            collection = new ArrayList<TypeCollection>();
        }
        return this.collection;
    }

    /**
     * Ruft den Wert der departmentsAndSeries-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TypeDepartmentsAndSeries }
     *     
     */
    public TypeDepartmentsAndSeries getDepartmentsAndSeries() {
        return departmentsAndSeries;
    }

    /**
     * Legt den Wert der departmentsAndSeries-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeDepartmentsAndSeries }
     *     
     */
    public void setDepartmentsAndSeries(TypeDepartmentsAndSeries value) {
        this.departmentsAndSeries = value;
    }

}
