//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.11.18 um 08:02:10 PM CET 
//


package gov.loc.marc21.slim;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für recordTypeType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="recordTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="Bibliographic"/>
 *     &lt;enumeration value="Authority"/>
 *     &lt;enumeration value="Holdings"/>
 *     &lt;enumeration value="Classification"/>
 *     &lt;enumeration value="Community"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "recordTypeType")
@XmlEnum
public enum RecordTypeType {

    @XmlEnumValue("Bibliographic")
    BIBLIOGRAPHIC("Bibliographic"),
    @XmlEnumValue("Authority")
    AUTHORITY("Authority"),
    @XmlEnumValue("Holdings")
    HOLDINGS("Holdings"),
    @XmlEnumValue("Classification")
    CLASSIFICATION("Classification"),
    @XmlEnumValue("Community")
    COMMUNITY("Community");
    private final String value;

    RecordTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RecordTypeType fromValue(String v) {
        for (RecordTypeType c: RecordTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
