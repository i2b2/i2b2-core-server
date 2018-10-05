/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;

import java.util.Date;

public class UserLoginData {
	private String userID;
	private String attemptCD;
	private Date entryDate;

	
	public String getUserID() {
		return userID;
	}
	public String getAttemptCD() {
		return attemptCD;
	}
	public void setAttemptCD(String attemptCD) {
		this.attemptCD = attemptCD;
	}
	public Date getEntryDate() {
		return entryDate;
	}
	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
}


