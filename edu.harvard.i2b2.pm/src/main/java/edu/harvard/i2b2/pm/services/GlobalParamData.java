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

public class GlobalParamData {
    // every persistent object needs an identifier

    public GlobalParamDataPK getGlobalParamDataPK() {
		return globalParamDataPK;
	}
	public void setGlobalParamDataPK(GlobalParamDataPK globalParamDataPK) {
		this.globalParamDataPK = globalParamDataPK;
	}
	private GlobalParamDataPK globalParamDataPK = new GlobalParamDataPK();

	
    private String status = new String();
    private Date changeDate = new Date();
    
	public Date getChangeDate() {
		return changeDate;
	}
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
    private String value = new String();
    private Boolean can_override = null;
    
	public String getOwner_id() {
		return globalParamDataPK.getOwner();
	}
	public void setOwner_id(String owner_id) {
		globalParamDataPK.setOwner(owner_id);
	}
	public Boolean getCan_override() {
		return can_override;
	}
	public void setCan_override(Boolean can_override) {
		this.can_override = can_override;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return globalParamDataPK.getName();
	}
	public void setName(String name) {
		globalParamDataPK.setName(name);
	}
	public String getProject() {
		return globalParamDataPK.getProject();
	}
	public void setProject(String project) {
		globalParamDataPK.setProject(project);
	}

}
