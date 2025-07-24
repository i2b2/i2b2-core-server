/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;


///import edu.harvard.i2b2.pm.datavo.pm.ParamType;

public class EnvironmentData {
    // every persistent object needs an identifier
    
	//private String oid = null;
    private String url = new String();
    private String domain = new String();
    private String oid = new String();
    private String environment = new String();
    private Boolean active = null;

	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
        public String getDomain() {
                return domain;
        }
        public void setDomain(String domain) {
                this.domain = domain;
        }
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}


}
