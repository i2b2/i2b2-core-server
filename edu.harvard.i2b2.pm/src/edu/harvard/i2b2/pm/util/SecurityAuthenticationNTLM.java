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

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;

public class SecurityAuthenticationNTLM implements SecurityAuthentication {

	@Override
	public boolean validateUser(String username, String password,
			Hashtable params) throws Exception {

		String domainController= "", domain= "";

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

	}

}
