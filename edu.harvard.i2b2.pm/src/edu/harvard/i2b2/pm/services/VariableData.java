/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;

public class VariableData {
    // every persistent object needs an identifier
	private VariableDataPK variableDataPK = new VariableDataPK();

    private String value = new String();
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return variableDataPK.getName();
	}
	public void setName(String name) {
		variableDataPK.setName(name);
	}
	public String getProject() {
		return variableDataPK.getProject();
	}
	public void setProject(String project) {
		variableDataPK.setProject(project);
	}
	public VariableDataPK getVariableDataPK() {
		return variableDataPK;
	}
	public void setVariableDataPK(VariableDataPK variableDataPK) {
		this.variableDataPK = variableDataPK;
	}

}
