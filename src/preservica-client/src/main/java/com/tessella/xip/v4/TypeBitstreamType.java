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
 * <p>Java-Klasse für typeBitstreamType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="typeBitstreamType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="File stream"/>
 *     &lt;enumeration value="Byte stream"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "typeBitstreamType")
@XmlEnum
public enum TypeBitstreamType {

    @XmlEnumValue("File stream")
    FILE_STREAM("File stream"),
    @XmlEnumValue("Byte stream")
    BYTE_STREAM("Byte stream");
    private final String value;

    TypeBitstreamType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypeBitstreamType fromValue(String v) {
        for (TypeBitstreamType c: TypeBitstreamType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
