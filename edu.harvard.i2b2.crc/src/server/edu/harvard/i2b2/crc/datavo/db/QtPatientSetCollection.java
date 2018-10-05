/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.datavo.db;
// Generated Oct 10, 2006 5:52:18 PM by Hibernate Tools 3.1.0.beta5



/**
 * QtPatientSetCollection generated by hbm2java
 */
public class QtPatientSetCollection  implements java.io.Serializable {

    // Fields    

     private long patientSetCollId;
     private QtQueryResultInstance qtQueryResultInstance;
     private Integer setIndex;
     private Long patientId;

     // Constructors

    /** default constructor */
    public QtPatientSetCollection() {
    }

	/** minimal constructor */
    public QtPatientSetCollection(long patientSetCollId) {
        this.patientSetCollId = patientSetCollId;
    }
    /** full constructor */
    public QtPatientSetCollection(long patientSetCollId, QtQueryResultInstance qtQueryResultInstance, Integer setIndex, Long patientId) {
       this.patientSetCollId = patientSetCollId;
       this.qtQueryResultInstance = qtQueryResultInstance;
       this.setIndex = setIndex;
       this.patientId = patientId;
    }
    
   
    // Property accessors
    public long getPatientSetCollId() {
        return this.patientSetCollId;
    }
    
    public void setPatientSetCollId(long patientSetCollId) {
        this.patientSetCollId = patientSetCollId;
    }
    public QtQueryResultInstance getQtQueryResultInstance() {
        return this.qtQueryResultInstance;
    }
    
    public void setQtQueryResultInstance(QtQueryResultInstance qtQueryResultInstance) {
        this.qtQueryResultInstance = qtQueryResultInstance;
    }
    public Integer getSetIndex() {
        return this.setIndex;
    }
    
    public void setSetIndex(Integer setIndex) {
        this.setIndex = setIndex;
    }
    public Long getPatientId() {
        return this.patientId;
    }
    
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }




}


