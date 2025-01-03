/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.10.27 at 11:21:39 AM EDT 
//


package jaxb;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for headerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="headerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://i2b2.mgh.harvard.edu/querytool}userType"/>
 *         &lt;element name="data_source" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="patient_set_limit" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="estimated_time" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="create_date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="submit_date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="complete_date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="request_type" type="{http://i2b2.mgh.harvard.edu/querytool}request_typeType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "headerType", propOrder = {
    "user",
    "dataSource",
    "patientSetLimit",
    "estimatedTime",
    "createDate",
    "submitDate",
    "completeDate",
    "requestType"
})
public class HeaderType {

    @XmlElement(required = true)
    protected UserType user;
    @XmlElement(name = "data_source", required = true)
    protected String dataSource;
    @XmlElement(name = "patient_set_limit")
    protected int patientSetLimit;
    @XmlElement(name = "estimated_time")
    protected int estimatedTime;
    @XmlElement(name = "create_date", required = true)
    protected XMLGregorianCalendar createDate;
    @XmlElement(name = "submit_date", required = true)
    protected XMLGregorianCalendar submitDate;
    @XmlElement(name = "complete_date", required = true)
    protected XMLGregorianCalendar completeDate;
    @XmlElement(name = "request_type", required = true)
    protected RequestTypeType requestType;

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link UserType }
     *     
     */
    public UserType getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserType }
     *     
     */
    public void setUser(UserType value) {
        this.user = value;
    }

    /**
     * Gets the value of the dataSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataSource(String value) {
        this.dataSource = value;
    }

    /**
     * Gets the value of the patientSetLimit property.
     * 
     */
    public int getPatientSetLimit() {
        return patientSetLimit;
    }

    /**
     * Sets the value of the patientSetLimit property.
     * 
     */
    public void setPatientSetLimit(int value) {
        this.patientSetLimit = value;
    }

    /**
     * Gets the value of the estimatedTime property.
     * 
     */
    public int getEstimatedTime() {
        return estimatedTime;
    }

    /**
     * Sets the value of the estimatedTime property.
     * 
     */
    public void setEstimatedTime(int value) {
        this.estimatedTime = value;
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
     * Gets the value of the submitDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSubmitDate() {
        return submitDate;
    }

    /**
     * Sets the value of the submitDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSubmitDate(XMLGregorianCalendar value) {
        this.submitDate = value;
    }

    /**
     * Gets the value of the completeDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCompleteDate() {
        return completeDate;
    }

    /**
     * Sets the value of the completeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCompleteDate(XMLGregorianCalendar value) {
        this.completeDate = value;
    }

    /**
     * Gets the value of the requestType property.
     * 
     * @return
     *     possible object is
     *     {@link RequestTypeType }
     *     
     */
    public RequestTypeType getRequestType() {
        return requestType;
    }

    /**
     * Sets the value of the requestType property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestTypeType }
     *     
     */
    public void setRequestType(RequestTypeType value) {
        this.requestType = value;
    }

}
