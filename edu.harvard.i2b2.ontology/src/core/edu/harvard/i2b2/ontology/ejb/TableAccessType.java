/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.ontology.ejb;

public class TableAccessType {

	private String tableCd; 
	private String tableName;
	private String fullName;
	private String synonymCd;
	private String visualAttributes;
	private String dimCode;
	private String name;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTableCd() {
		return tableCd;
	}
	public void setTableCd(String tableCd) {
		this.tableCd = tableCd;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getSynonymCd() {
		return synonymCd;
	}
	public void setSynonymCd(String synonymCd) {
		this.synonymCd = synonymCd;
	}
	public String getVisualAttributes() {
		return visualAttributes;
	}
	public void setVisualAttributes(String visualAttributes) {
		this.visualAttributes = visualAttributes;
	}
	public String getDimCode() {
		return dimCode;
	}
	public void setDimCode(String dimCode) {
		this.dimCode = dimCode;
	}
	
	
	
	
}
