//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.08.22 at 10:48:46 AM EDT 
//


package edu.harvard.i2b2.crc.datavo.setfinder.query;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scheduleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scheduleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repeat" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="intervalIn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Interval" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scheduleType", propOrder = {
    "repeat",
    "intervalIn",
    "interval"
})
public class ScheduleType {

    @XmlElement(required = true)
    protected String repeat;
    @XmlElement(required = true)
    protected String intervalIn;
    @XmlElement(name = "Interval")
    protected int interval;

    /**
     * Gets the value of the repeat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepeat() {
        return repeat;
    }

    /**
     * Sets the value of the repeat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepeat(String value) {
        this.repeat = value;
    }

    /**
     * Gets the value of the intervalIn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIntervalIn() {
        return intervalIn;
    }

    /**
     * Sets the value of the intervalIn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIntervalIn(String value) {
        this.intervalIn = value;
    }

    /**
     * Gets the value of the interval property.
     * 
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Sets the value of the interval property.
     * 
     */
    public void setInterval(int value) {
        this.interval = value;
    }

}
