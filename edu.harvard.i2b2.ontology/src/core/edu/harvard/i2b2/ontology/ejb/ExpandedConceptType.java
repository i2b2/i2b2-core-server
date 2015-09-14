/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
 package edu.harvard.i2b2.ontology.ejb;

import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;

public class ExpandedConceptType extends ConceptType{

	private String tableCd;
	
	public ExpandedConceptType() {
		super();
	}
	
	public void setTableCd(String code){
		tableCd = code;
	}
	
	public String getTableCd(){
		return tableCd; 
	}
}
