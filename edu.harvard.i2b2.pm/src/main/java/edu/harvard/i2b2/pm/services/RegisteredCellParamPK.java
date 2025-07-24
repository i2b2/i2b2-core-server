/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;

import java.io.Serializable;

public class RegisteredCellParamPK  implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    // every persistent object needs an identifier
    private String cellid = new String(); 
    private String name = new String();
    private String owner_id = new String();
    private String project_path = new String();

    

    public RegisteredCellParamPK() {
    }

    public RegisteredCellParamPK(RegisteredCellParamPK registeredCellParamPK) {
    	this.owner_id = registeredCellParamPK.owner_id;
    	this.cellid = registeredCellParamPK.cellid;
    	this.name = registeredCellParamPK.name;
    	this.project_path = registeredCellParamPK.project_path;
    }
    
    public boolean equals(RegisteredCellParamPK registeredCellParamPK) {
    	return (this.owner_id.equals(registeredCellParamPK.owner_id) &&
    	this.cellid.equals(registeredCellParamPK.cellid) &&
    	this.name.equals(registeredCellParamPK.name) &&
    	this.project_path.equals(registeredCellParamPK.project_path));
    	}
   
    public boolean equals(Object obj) {
    	if(this == obj)
    	return true;
    	if((obj == null) || (obj.getClass() != this.getClass()))
    	return false;
    	// object must be Test at this point
    	RegisteredCellParamPK registeredCellParamPK = (RegisteredCellParamPK)obj;
    	return (this.owner_id.equals(registeredCellParamPK.owner_id) &&
    	this.cellid.equals(registeredCellParamPK.cellid) &&
    	this.name.equals(registeredCellParamPK.name) &&
    	this.project_path.equals(registeredCellParamPK.project_path));
    	}
    public int hashCode () {
    	return new HashCodeBuilder().
    	append(getOwner_id()).
    	append(getCellid()).
    	append(getName()).
    	append(getProject_path()).getHashCode();
    	}
	public String getOwner_id() {
		return owner_id;
	}
	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}
	public String getProject_path() {
		return project_path;
	}
	public void setProject_path(String project_path) {
		this.project_path = project_path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCellid() {
		return cellid;
	}
	public void setCellid(String cellid) {
		this.cellid = cellid;
	}
}
