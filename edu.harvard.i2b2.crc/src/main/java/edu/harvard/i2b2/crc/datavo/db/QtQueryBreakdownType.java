/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.datavo.db;


import java.util.Date;

/**
 * QtQueryBreakdownType 
 */
public class QtQueryBreakdownType implements java.io.Serializable {

	// Fields

	private String name;
	private String value;
	private Date createDate;
	private Date updateDate;
	private String userId;
	private String userRoleCd;
	private String classname;

	// Constructors

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getUserRoleCd() {
		return userRoleCd;
	}

	public void setUserRoleCd(String userRoleCd) {
		this.userRoleCd = userRoleCd;
	}

	/** default constructor */
	public QtQueryBreakdownType() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
