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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for patient_enc_collectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="patient_enc_collectionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="patient_enc_coll_id" type="{http://i2b2.mgh.harvard.edu/querytool}patient_enc_coll_idType"/>
 *         &lt;element name="result_instance_id" type="{http://i2b2.mgh.harvard.edu/querytool}result_instance_idType"/>
 *         &lt;element name="set_index" type="{http://i2b2.mgh.harvard.edu/querytool}set_indexType"/>
 *         &lt;element name="patient_num" type="{http://i2b2.mgh.harvard.edu/querytool}patient_numType"/>
 *         &lt;element name="encounter_num" type="{http://i2b2.mgh.harvard.edu/querytool}encounter_numType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "patient_enc_collectionType", propOrder = {
    "patientEncCollId",
    "resultInstanceId",
    "setIndex",
    "patientNum",
    "encounterNum"
})
public class PatientEncCollectionType {

    @XmlElement(name = "patient_enc_coll_id")
    protected int patientEncCollId;
    @XmlElement(name = "result_instance_id")
    protected int resultInstanceId;
    @XmlElement(name = "set_index")
    protected int setIndex;
    @XmlElement(name = "patient_num")
    protected int patientNum;
    @XmlElement(name = "encounter_num")
    protected int encounterNum;

    /**
     * Gets the value of the patientEncCollId property.
     * 
     */
    public int getPatientEncCollId() {
        return patientEncCollId;
    }

    /**
     * Sets the value of the patientEncCollId property.
     * 
     */
    public void setPatientEncCollId(int value) {
        this.patientEncCollId = value;
    }

    /**
     * Gets the value of the resultInstanceId property.
     * 
     */
    public int getResultInstanceId() {
        return resultInstanceId;
    }

    /**
     * Sets the value of the resultInstanceId property.
     * 
     */
    public void setResultInstanceId(int value) {
        this.resultInstanceId = value;
    }

    /**
     * Gets the value of the setIndex property.
     * 
     */
    public int getSetIndex() {
        return setIndex;
    }

    /**
     * Sets the value of the setIndex property.
     * 
     */
    public void setSetIndex(int value) {
        this.setIndex = value;
    }

    /**
     * Gets the value of the patientNum property.
     * 
     */
    public int getPatientNum() {
        return patientNum;
    }

    /**
     * Sets the value of the patientNum property.
     * 
     */
    public void setPatientNum(int value) {
        this.patientNum = value;
    }

    /**
     * Gets the value of the encounterNum property.
     * 
     */
    public int getEncounterNum() {
        return encounterNum;
    }

    /**
     * Sets the value of the encounterNum property.
     * 
     */
    public void setEncounterNum(int value) {
        this.encounterNum = value;
    }

}
