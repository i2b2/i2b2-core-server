package edu.harvard.i2b2.crc.exec;

import java.security.Permission;

public class MySecurityManager extends SecurityManager {
	  @Override public void checkExit(int status) {
		    throw new SecurityException();
		  }

		  @Override public void checkPermission(Permission perm) {
		      // Allow other activities by default
		  }
		}