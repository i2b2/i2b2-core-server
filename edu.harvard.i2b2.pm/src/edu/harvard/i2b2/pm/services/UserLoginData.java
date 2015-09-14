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


