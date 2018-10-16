//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.16 um 03:18:14 PM CET 
//


package com.tessella.xip.v4;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für typeStatus.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="typeStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="new"/>
 *     &lt;enumeration value="same"/>
 *     &lt;enumeration value="changed"/>
 *     &lt;enumeration value="deleted"/>
 *     &lt;enumeration value="restored"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "typeStatus")
@XmlEnum
public enum TypeStatus {

    @XmlEnumValue("new")
    NEW("new"),
    @XmlEnumValue("same")
    SAME("same"),
    @XmlEnumValue("changed")
    CHANGED("changed"),
    @XmlEnumValue("deleted")
    DELETED("deleted"),
    @XmlEnumValue("restored")
    RESTORED("restored");
    private final String value;

    TypeStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypeStatus fromValue(String v) {
        for (TypeStatus c: TypeStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
