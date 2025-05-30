//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.17 at 02:57:48 PM EDT 
//


package edu.harvard.i2b2.im.datavo.setfinder.query;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for constrainOperatorType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="constrainOperatorType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EQ"/>
 *     &lt;enumeration value="NE"/>
 *     &lt;enumeration value="GT"/>
 *     &lt;enumeration value="LT"/>
 *     &lt;enumeration value="GE"/>
 *     &lt;enumeration value="LE"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="LIKE"/>
 *     &lt;enumeration value="LIKE[begin]"/>
 *     &lt;enumeration value="LIKE[end]"/>
 *     &lt;enumeration value="LIKE[contains]"/>
 *     &lt;enumeration value="LIKE[exact]"/>
 *     &lt;enumeration value="BETWEEN"/>
 *     &lt;enumeration value="CONTAINS"/>
 *     &lt;enumeration value="CONTAINS[database]"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "constrainOperatorType", namespace = "http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/")
@XmlEnum
public enum ConstrainOperatorType {

    EQ("EQ"),
    NE("NE"),
    GT("GT"),
    LT("LT"),
    GE("GE"),
    LE("LE"),
    IN("IN"),
    LIKE("LIKE"),
    @XmlEnumValue("LIKE[begin]")
    LIKE_BEGIN("LIKE[begin]"),
    @XmlEnumValue("LIKE[end]")
    LIKE_END("LIKE[end]"),
    @XmlEnumValue("LIKE[contains]")
    LIKE_CONTAINS("LIKE[contains]"),
    @XmlEnumValue("LIKE[exact]")
    LIKE_EXACT("LIKE[exact]"),
    BETWEEN("BETWEEN"),
    CONTAINS("CONTAINS"),
    @XmlEnumValue("CONTAINS[database]")
    CONTAINS_DATABASE("CONTAINS[database]");
    private final String value;

    ConstrainOperatorType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConstrainOperatorType fromValue(String v) {
        for (ConstrainOperatorType c: ConstrainOperatorType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
