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
