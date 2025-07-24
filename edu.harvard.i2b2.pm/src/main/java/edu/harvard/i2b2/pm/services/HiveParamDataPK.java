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

public class HiveParamDataPK implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// every persistent object needs an identifier

    private String name = new String();
    private String domain = new String();

    public HiveParamDataPK() {
    }

    public HiveParamDataPK(HiveParamDataPK hiveParamDataPK) {
    	this.name = hiveParamDataPK.name;
    	this.domain = hiveParamDataPK.domain;
    }
    
    public boolean equals(HiveParamDataPK hiveParamDataPK) {
    	return (this.name.equals(hiveParamDataPK.name) &&
    	this.domain.equals(hiveParamDataPK.domain) );
    	}
   
    public boolean equals(Object obj) {
    	if(this == obj)
    	return true;
    	if((obj == null) || (obj.getClass() != this.getClass()))
    	return false;
    	// object must be Test at this point
    	HiveParamDataPK hiveParamDataPK = (HiveParamDataPK)obj;
    	return (this.name.equals(hiveParamDataPK.name) &&
    	this.domain.equals(hiveParamDataPK.domain));
    	}
    public int hashCode () {
    	return new HashCodeBuilder().
    	append(getName()).
    	append(getDomain()).getHashCode();
    	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}


}
