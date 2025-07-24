/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;


public interface IEncounterSetCollectionDao {

	/**
	 * function to add encounter to encounter set without out creating new db
	 * session
	 * 
	 * @param patientId
	 */
	public void addEncounter(long encounterId, long patientId);

	/**
	 * Set resultInstance before addEncounter
	 * 
	 * @param resultInstanceId
	 */
	public void createPatientEncCollection(String resultInstanceId);

	public String getResultInstanceId();

	/**
	 * Call this function at the end. i.e. after loading all patient with
	 * addPatient function, finally call this function to clear session
	 */
	public void flush();

}
