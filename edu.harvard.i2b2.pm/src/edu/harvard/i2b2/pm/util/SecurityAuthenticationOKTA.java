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

import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.authn.sdk.resource.AuthenticationResponse;


public class SecurityAuthenticationOKTA implements SecurityAuthentication {

	@Override
	public boolean validateUser(String username, String password,
			Hashtable params) throws Exception {

		String domainController= "", domain= "";

		
		AuthenticationClient client = AuthenticationClients.builder()
			    .setOrgUrl("https://" +  (String) params.get("domain"))
			    .build();
		
		
		String relayState = "/application/specific";
		
		char[] c_password = password.toCharArray(); 
		
		//client.au
		try {
		client.authenticate(username, c_password, relayState, new ExampleAuthenticationStateHandler());
		//UniAddress mydomaincontroller = UniAddress.getByName( (String) params.get("domain_controller") );
		//NtlmPasswordAuthentication mycreds = new NtlmPasswordAuthentication( (String) params.get("domain"), username, password );
/*
		
		AccessTokenVerifier jwtVerifier = JwtVerifiers.accessTokenVerifierBuilder()
			      .setIssuer("https://" +  (String) params.get("domain") + "/oauth2/default")
			      .setAudience("api://default")      // defaults to 'api://default'
			      .setConnectionTimeout(Duration.ofSeconds(1)) // defaults to 1000ms
			      .setReadTimeout(Duration.ofSeconds(1))       // defaults to 1000ms
			      .build();
		try {
			Jwt jwt = jwtVerifier.decode(token);
			
		       System.out.println(jwt.getTokenValue()); // print the token
		        System.out.println(jwt.getClaims().get("invalidKey")); // an invalid key just returns null
		        System.out.println(jwt.getClaims().get("groups")); // handle an array value
		        System.out.println(jwt.getExpiresAt()); // print the expiration time
		//	SmbSession.logon( mydomaincontroller, mycreds );
		 * */
			// SUCCESS
			return true;
		} catch( Exception se ) {
			// NETWORK PROBLEMS?
			se.printStackTrace();
			throw new Exception (se.getMessage());
		}

	}

}

 class ExampleAuthenticationStateHandler extends AuthenticationStateHandlerAdapter {

    @Override
    public void handleUnknown(AuthenticationResponse unknownResponse) {
        // redirect to "/error"
    }

    @Override
    public void handleSuccess(AuthenticationResponse successResponse) {
        
        // a user is ONLY considered authenticated if a sessionToken exists
    	//String test = successResponse.getSessionToken();
  //      if (successResponse != null) {
    //        String relayState = successResponse.getRelayState();
      //      String dest = relayState != null ? relayState : "/";
            // redirect to dest    
        //}
        // other state transition successful 
    }

    @Override
    public void handlePasswordExpired(AuthenticationResponse passwordExpired) {
        // redirect to "/login/change-password"
    }
    
    // Other implemented states here
}
