/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;

public class UserParamData {

    private int id = -1;

	private String datatype = new String();

	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	private String value = new String();
    public UserParamDataPK getUserParamDataPK() {
		return userParamDataPK;
	}
	public void setUserParamDataPK(UserParamDataPK userParamDataPK) {
		this.userParamDataPK = userParamDataPK;
	}
	private UserParamDataPK userParamDataPK = new UserParamDataPK();
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return userParamDataPK.getName();
	}
	public void setName(String name) {
		userParamDataPK.setName(name);
	}
	public String getUser() {
		return userParamDataPK.getUser();
	}
	public void setUser(String user) {
		userParamDataPK.setUser(user);
	}



}
