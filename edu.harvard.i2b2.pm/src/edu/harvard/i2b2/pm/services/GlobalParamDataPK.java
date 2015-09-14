package edu.harvard.i2b2.pm.services;

import java.io.Serializable;

public class GlobalParamDataPK implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// every persistent object needs an identifier

    private String name = new String();
    private String owner = new String();
    private String project = new String();

    public GlobalParamDataPK() {
    }

    public GlobalParamDataPK(GlobalParamDataPK globalParamDataPK) {
    	this.name = globalParamDataPK.name;
    	this.owner = globalParamDataPK.owner;
    	this.project = globalParamDataPK.project;
    }
    
    public boolean equals(GlobalParamDataPK globalParamDataPK) {
    	return (this.name.equals(globalParamDataPK.name) &&
    			this.owner.equals(globalParamDataPK.owner) &&
    	this.project.equals(globalParamDataPK.project) );
    	}
   
    public boolean equals(Object obj) {
    	if(this == obj)
    	return true;
    	if((obj == null) || (obj.getClass() != this.getClass()))
    	return false;
    	// object must be Test at this point
    	GlobalParamDataPK globalParamDataPK = (GlobalParamDataPK)obj;
    	return (this.name.equals(globalParamDataPK.name) &&
    			this.owner.equals(globalParamDataPK.owner) &&
    	this.project.equals(globalParamDataPK.project));
    	}
    public int hashCode () {
    	return new HashCodeBuilder().
    	append(getName()).
    	append(getOwner()).
    	append(getProject()).getHashCode();
    	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}



}
