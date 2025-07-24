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

public class GroupData {
    // every persistent object needs an identifier
    
	//private String oid = null;
    private String wiki = new String();
    private String oid = new String();
    private String name = new String();
    private String key = new String();
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getWiki() {
		return wiki;
	}
	public void setWiki(String wiki) {
		this.wiki = wiki;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}

}
