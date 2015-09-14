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
