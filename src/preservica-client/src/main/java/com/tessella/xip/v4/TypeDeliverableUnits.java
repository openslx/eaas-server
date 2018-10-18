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
 * <p>Java-Klasse für typeDeliverableUnits complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="typeDeliverableUnits">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DeliverableUnit" type="{http://www.tessella.com/XIP/v4}typeDeliverableUnit" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="RelatedDeliverableUnit" type="{http://www.tessella.com/XIP/v4}typeRelatedDeliverableUnit" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Manifestation" type="{http://www.tessella.com/XIP/v4}typeManifestation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeDeliverableUnits", propOrder = {
    "deliverableUnit",
    "relatedDeliverableUnit",
    "manifestation"
})
public class TypeDeliverableUnits {

    @XmlElement(name = "DeliverableUnit")
    protected List<TypeDeliverableUnit> deliverableUnit;
    @XmlElement(name = "RelatedDeliverableUnit")
    protected List<TypeRelatedDeliverableUnit> relatedDeliverableUnit;
    @XmlElement(name = "Manifestation")
    protected List<TypeManifestation> manifestation;

    /**
     * Gets the value of the deliverableUnit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deliverableUnit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeliverableUnit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeDeliverableUnit }
     * 
     * 
     */
    public List<TypeDeliverableUnit> getDeliverableUnit() {
        if (deliverableUnit == null) {
            deliverableUnit = new ArrayList<TypeDeliverableUnit>();
        }
        return this.deliverableUnit;
    }

    /**
     * Gets the value of the relatedDeliverableUnit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relatedDeliverableUnit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelatedDeliverableUnit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeRelatedDeliverableUnit }
     * 
     * 
     */
    public List<TypeRelatedDeliverableUnit> getRelatedDeliverableUnit() {
        if (relatedDeliverableUnit == null) {
            relatedDeliverableUnit = new ArrayList<TypeRelatedDeliverableUnit>();
        }
        return this.relatedDeliverableUnit;
    }

    /**
     * Gets the value of the manifestation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the manifestation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManifestation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeManifestation }
     * 
     * 
     */
    public List<TypeManifestation> getManifestation() {
        if (manifestation == null) {
            manifestation = new ArrayList<TypeManifestation>();
        }
        return this.manifestation;
    }

}
