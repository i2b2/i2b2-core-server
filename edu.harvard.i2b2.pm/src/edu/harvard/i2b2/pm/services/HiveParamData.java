/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;

public class HiveParamData {

    private String value = new String();
    public HiveParamDataPK getHiveParamDataPK() {
		return hiveParamDataPK;
	}
	public void setHiveParamDataPK(HiveParamDataPK hiveParamDataPK) {
		this.hiveParamDataPK = hiveParamDataPK;
	}
	private HiveParamDataPK hiveParamDataPK = new HiveParamDataPK();
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return hiveParamDataPK.getName();
	}
	public void setName(String name) {
		hiveParamDataPK.setName(name);
	}
	public String getDomain() {
		return hiveParamDataPK.getDomain();
	}
	public void setDomain(String domain) {
		hiveParamDataPK.setDomain(domain);
	}



}
