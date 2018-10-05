/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;

public class RegisteredCellParam {
    // every persistent object needs an identifier
    private String value = new String();
    private Boolean can_override = null;
	private String project_path = new String();
   	private RegisteredCellParamPK registeredCellParamPK = new RegisteredCellParamPK();

    public RegisteredCellParamPK getRegisteredCellParamPK() {
		return registeredCellParamPK;
	}
	public void setRegisteredCellParamPK(RegisteredCellParamPK registeredCellParamPK) {
		this.registeredCellParamPK = registeredCellParamPK;
	}

    public String getValue() {
		return value;
	}
	public Boolean getCan_override() {
		return can_override;
	}
	public void setCan_override(Boolean can_override) {
		this.can_override = can_override;
	}
	public String getOwner_id() {
		return registeredCellParamPK.getOwner_id();
	}
	public void setOwner_id(String owner_id) {
		registeredCellParamPK.setOwner_id(owner_id);
	}
	public String getProject_path() {
		return registeredCellParamPK.getProject_path();
	}
	public void setProject_path(String project_path) {
		registeredCellParamPK.setProject_path(project_path);
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return registeredCellParamPK.getName();
	}
	public void setName(String name) {
		registeredCellParamPK.setName(name);
	}
	public String getCellid() {
		return registeredCellParamPK.getCellid();
	}
	public void setCellid(String cellid) {
		registeredCellParamPK.setCellid(cellid);
	}
}
