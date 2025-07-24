/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors:
 * 		Lori Phillips
 */
 package edu.harvard.i2b2.im.ejb;

import edu.harvard.i2b2.im.util.StringUtil;


public class NodeType {

	protected String node;
	protected String type;
	protected Boolean blob;
	
	
    public boolean isBlob() {
        if (blob == null) {
            return false;
        } else {
            return blob;
        }
    }
    
	public void setBlob(Boolean value) {
		this.blob = value;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public NodeType() {}
	/*
	
	public NodeType(GetChildrenType childrenType) {
		node = StringUtil.getTableCd(childrenType.getParent());
		if (childrenType.getType().equals("default")){
			type = "core";
		}else {
			type = childrenType.getType();
		}
		blob = childrenType.isBlob();
	}
	*/
}
