package edu.harvard.i2b2.crc.loader.datavo.loader;

import java.io.Serializable;


/**
 * User profile data object
 * @author rk903
 *
 */
public class UserProfile implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private String userId = null;
	private String password = null;
	
	
	public UserProfile() { 
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}

}
