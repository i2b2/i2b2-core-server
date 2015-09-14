package edu.harvard.i2b2.pm.services;

public class ProjectUserParamData {
    // every persistent object needs an identifier
//    private String oid = null;

    private String value = new String();
    public ProjectUserParamDataPK getProjectUserParamDataPK() {
		return projectUserParamDataPK;
	}
	public void setProjectUserParamDataPK(ProjectUserParamDataPK projectUserParamDataPK) {
		this.projectUserParamDataPK = projectUserParamDataPK;
	}
	private ProjectUserParamDataPK projectUserParamDataPK = new ProjectUserParamDataPK();
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return projectUserParamDataPK.getName();
	}
	public void setName(String name) {
		projectUserParamDataPK.setName(name);
	}
	public String getUser() {
		return projectUserParamDataPK.getUser();
	}
	public void setUser(String user) {
		projectUserParamDataPK.setUser(user);
	}
	public String getProject() {
		return projectUserParamDataPK.getProject();
	}
	public void setProject(String project) {
		projectUserParamDataPK.setProject(project);
	}


}
