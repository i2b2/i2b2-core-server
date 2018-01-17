package edu.harvard.i2b2.pm.util;

import java.util.Hashtable;

public interface SecurityAuthentication {
	
	boolean validateUser(String username, String password, Hashtable params) throws Exception;
}
