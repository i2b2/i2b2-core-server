package edu.harvard.i2b2.pm.util;

import java.util.Hashtable;
import java.util.List;

import edu.harvard.i2b2.pm.ejb.DBInfoType;

public interface SecurityAuthentication {
	
	boolean validateUser(String username, String password, Hashtable params) throws Exception;
}
