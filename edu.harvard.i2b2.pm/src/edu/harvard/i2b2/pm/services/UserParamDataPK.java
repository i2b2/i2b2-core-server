package edu.harvard.i2b2.pm.services;

import java.io.Serializable;

public class UserParamDataPK implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// every persistent object needs an identifier

    private String name = new String();
    private String user = new String();

    public UserParamDataPK() {
    }

    public UserParamDataPK(UserParamDataPK userParamDataPK) {
    	this.name = userParamDataPK.name;
    	this.user = userParamDataPK.user;
    }
    
    public boolean equals(UserParamDataPK userParamDataPK) {
    	return (this.name.equals(userParamDataPK.name) &&
    	this.user.equals(userParamDataPK.user) );
    	}
   
    public boolean equals(Object obj) {
    	if(this == obj)
    	return true;
    	if((obj == null) || (obj.getClass() != this.getClass()))
    	return false;
    	// object must be Test at this point
    	UserParamDataPK userParamDataPK = (UserParamDataPK)obj;
    	return (this.name.equals(userParamDataPK.name) &&
    	this.user.equals(userParamDataPK.user));
    	}
    public int hashCode () {
    	return new HashCodeBuilder().
    	append(getName()).
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


}
