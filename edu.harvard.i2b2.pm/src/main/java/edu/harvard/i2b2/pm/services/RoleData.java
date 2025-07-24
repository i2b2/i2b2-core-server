/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;

public class RoleData {
    // every persistent object needs an identifier
    
	private RoleDataPK roleDataPK = new RoleDataPK();

	public RoleDataPK getRoleDataPK() {
		return roleDataPK;
	}
	public void setRoleDataPK(RoleDataPK roleDataPK) {
		this.roleDataPK = roleDataPK;
	}
	public String getProject() {
		return roleDataPK.getProject();
	}
	public void setProject(String project) {
		roleDataPK.setProject(project);
	}
	public String getRole() {
		return roleDataPK.getRole();
	}
	public void setRole(String role) {
		roleDataPK.setRole(role);
	}
	public String getUser() {
		return roleDataPK.getUser();
	}
	public void setUser(String user) {
		roleDataPK.setUser(user);
	}
}
