package edu.harvard.i2b2.pm.services;

import java.io.Serializable;

public class ProjectUserParamDataPK implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// every persistent object needs an identifier

    private String name = new String();
    private String project = new String();
    private String user = new String();

    public ProjectUserParamDataPK() {
    }

    public ProjectUserParamDataPK(ProjectUserParamDataPK userDataPK) {
    	this.name = userDataPK.name;
    	this.project = userDataPK.project;
    	this.user = userDataPK.user;
    }
    
    public boolean equals(ProjectUserParamDataPK userDataPK) {
    	return (this.name.equals(userDataPK.name) &&
    	this.project.equals(userDataPK.project) &&
    	this.user.equals(userDataPK.user));
    	}
   
    public boolean equals(Object obj) {
    	if(this == obj)
    	return true;
    	if((obj == null) || (obj.getClass() != this.getClass()))
    	return false;
    	// object must be Test at this point
    	ProjectUserParamDataPK userDataPK = (ProjectUserParamDataPK)obj;
    	return (this.name.equals(userDataPK.name) &&
    	this.project.equals(userDataPK.project) &&
    	this.user.equals(userDataPK.user));
    	}
    public int hashCode () {
    	return new HashCodeBuilder().
    	append(getName()).
    	append(getProject()).
    	append(getUser()).getHashCode();
    	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}


}
