/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;

import java.util.HashSet;
import java.util.Set;


public class RegisteredCell {

    private String name = new String();
    private String url = new String();
    private String webservice = new String();
    private Boolean can_override = null;
    private Set params = new HashSet();

    
	public RegisteredCellPK getRegisteredCellPK() {
		return registeredCellPK;
	}
	public void setRegisteredCellPK(RegisteredCellPK registeredCellPK) {
		this.registeredCellPK = registeredCellPK;
	}
	private RegisteredCellPK registeredCellPK = new RegisteredCellPK();

    
    public Boolean getCan_override() {
		return can_override;
	}
	public void setCan_override(Boolean can_override) {
		this.can_override = can_override;
	}
	public String getOwner_id() {
		return registeredCellPK.getOwner_id();
	}
	public void setOwner_id(String owner_id) {
		registeredCellPK.setOwner_id(owner_id);
	}
	public String getProject_path() {
		return registeredCellPK.getProject_path();
	}
	public void setProject_path(String project_path) {
		registeredCellPK.setProject_path(project_path);
	}
	public String getWebservice() {
		return webservice;
	}
	public void setWebservice(String webservice) {
		this.webservice = webservice;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return registeredCellPK.getId();
	}
	public void setId(String id) {
		registeredCellPK.setId(id);
	}
	public Set getParams() {
		return params;
	}
	public void setParams(Set params) {
		this.params = params;
	}
}
