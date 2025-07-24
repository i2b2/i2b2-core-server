/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.util;

import java.util.Hashtable;
import java.util.Properties;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;


public class SecurityAuthenticationNTLM2 implements SecurityAuthentication {

	@Override
	public boolean validateUser(String username, String password,
			Hashtable params) throws Exception {

	
	    SMBClient client = new SMBClient();

	    try (
	    		Connection connection = client.connect((String) params.get("domain_controller"))) {
	        AuthenticationContext ac = new AuthenticationContext(username, password .toCharArray(), (String) params.get("domain"));
	        Session session = connection.authenticate(ac);

	        session.getConnection();
	        session.close();
	        return true;
	    } catch(Exception se ) {
			// NETWORK PROBLEMS?
			throw new Exception (se.getMessage());
		}
		
		
		/*
		UniAddress mydomaincontroller = UniAddress.getByName( (String) params.get("domain_controller") );
		NtlmPasswordAuthentication mycreds = new NtlmPasswordAuthentication( (String) params.get("domain"), username, password );
		try {

			SmbSession.logon( mydomaincontroller, mycreds );
			// SUCCESS
			return true;
		} catch( SmbAuthException sae ) {
			// AUTHENTICATION FAILURE
			throw new Exception (sae.getMessage());
		} catch( SmbException se ) {
			// NETWORK PROBLEMS?
			throw new Exception (se.getMessage());
		}
	*/
	}

}
