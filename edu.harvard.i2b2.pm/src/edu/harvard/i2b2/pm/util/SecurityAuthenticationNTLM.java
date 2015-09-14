package edu.harvard.i2b2.pm.util;

import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import edu.harvard.i2b2.pm.ejb.DBInfoType;
import edu.harvard.i2b2.pm.services.HiveParamData;

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
