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
 * <p>Java-Klasse für typeDepartmentsAndSeries complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeDepartmentsAndSeries">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Department" type="{http://www.tessella.com/XIP/v4}typeDepartment" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="RecordSeries" type="{http://www.tessella.com/XIP/v4}typeRecordSeries" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeDepartmentsAndSeries", propOrder = {
    "department",
    "recordSeries"
})
public class TypeDepartmentsAndSeries {

    @XmlElement(name = "Department")
    protected List<TypeDepartment> department;
    @XmlElement(name = "RecordSeries")
    protected List<TypeRecordSeries> recordSeries;

    /**
     * Gets the value of the department property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the department property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDepartment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeDepartment }
     * 
     * 
     */
    public List<TypeDepartment> getDepartment() {
        if (department == null) {
            department = new ArrayList<TypeDepartment>();
        }
        return this.department;
    }

    /**
     * Gets the value of the recordSeries property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recordSeries property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecordSeries().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeRecordSeries }
     * 
     * 
     */
    public List<TypeRecordSeries> getRecordSeries() {
        if (recordSeries == null) {
            recordSeries = new ArrayList<TypeRecordSeries>();
        }
        return this.recordSeries;
    }

}
