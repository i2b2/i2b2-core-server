//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.17 at 02:57:57 PM EDT 
//


package edu.harvard.i2b2.ontology.datavo.crc.setfinder.query;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for findBy_childType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="findBy_childType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="match_str" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}match_strType"/>
 *         &lt;element name="create_date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="user_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ascending" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *       &lt;attribute name="max" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="category" default="top">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="@"/>
 *             &lt;enumeration value="top"/>
 *             &lt;enumeration value="results"/>
 *             &lt;enumeration value="pdo"/>
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
@XmlType(name = "findBy_childType", propOrder = {
    "matchStr",
    "createDate",
    "userId",
    "ascending"
})
public class FindByChildType {

    @XmlElement(name = "match_str", required = true)
    protected MatchStrType matchStr;
    @XmlElement(name = "create_date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createDate;
    @XmlElement(name = "user_id", required = true)
    protected String userId;
    @XmlElement(defaultValue = "false")
    protected boolean ascending;
    @XmlAttribute(name = "max", required = true)
    protected int max;
    @XmlAttribute(name = "category")
    protected String category;

    /**
     * Gets the value of the matchStr property.
     * 
     * @return
     *     possible object is
     *     {@link MatchStrType }
     *     
     */
    public MatchStrType getMatchStr() {
        return matchStr;
    }

    /**
     * Sets the value of the matchStr property.
     * 
     * @param value
     *     allowed object is
     *     {@link MatchStrType }
     *     
     */
    public void setMatchStr(MatchStrType value) {
        this.matchStr = value;
    }

    /**
     * Gets the value of the createDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreateDate() {
        return createDate;
    }

    /**
     * Sets the value of the createDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreateDate(XMLGregorianCalendar value) {
        this.createDate = value;
    }

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * Gets the value of the ascending property.
     * 
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets the value of the ascending property.
     * 
     */
    public void setAscending(boolean value) {
        this.ascending = value;
    }

    /**
     * Gets the value of the max property.
     * 
     */
    public int getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     */
    public void setMax(int value) {
        this.max = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategory() {
        if (category == null) {
            return "top";
        } else {
            return category;
        }
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategory(String value) {
        this.category = value;
    }

}
