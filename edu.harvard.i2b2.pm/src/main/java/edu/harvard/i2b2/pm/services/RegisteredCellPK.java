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

public class RegisteredCellPK  implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    // every persistent object needs an identifier
    //private String oid = new String();

    private String owner_id = new String();
    private String id = new String();
    private String project_path = new String();
    

    public RegisteredCellPK() {
    }

    public RegisteredCellPK(RegisteredCellPK registeredCellPK) {
    	this.owner_id = registeredCellPK.owner_id;
    	this.id = registeredCellPK.id;
    	this.project_path = registeredCellPK.project_path;
    }
    
    public boolean equals(RegisteredCellPK registeredCellPK) {
    	return (this.owner_id.equals(registeredCellPK.owner_id) &&
    	this.id.equals(registeredCellPK.id) &&
    	this.project_path.equals(registeredCellPK.project_path));
    	}
   
    public boolean equals(Object obj) {
    	if(this == obj)
    	return true;
    	if((obj == null) || (obj.getClass() != this.getClass()))
    	return false;
    	// object must be Test at this point
    	RegisteredCellPK registeredCellPK = (RegisteredCellPK)obj;
    	return (this.owner_id.equals(registeredCellPK.owner_id) &&
    	this.id.equals(registeredCellPK.id) &&
    	this.project_path.equals(registeredCellPK.project_path));
    	}
    public int hashCode () {
    	return new HashCodeBuilder().
    	append(getOwner_id()).
    	append(getId()).
    	append(getProject_path()).getHashCode();
    	}

	public String getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProject_path() {
		return project_path;
	}

	public void setProject_path(String project_path) {
		this.project_path = project_path;
	}
    
    
}
