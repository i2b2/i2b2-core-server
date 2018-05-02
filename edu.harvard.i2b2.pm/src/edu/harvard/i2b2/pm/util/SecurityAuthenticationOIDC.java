package edu.harvard.i2b2.pm.util;

import com.auth0.jwk.*;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Hashtable;
import java.util.Map;

/*
 * OpenID Connect authentication for i2b2 v1.6
 * Supports JWT verification signed with RS256 and keys retrieved via JWKS.
 *
 * The parameters are listed below with their possible values in ():
 * authentication_method - (OIDC)
 * oidc_jwks_uri - () URI of JWKS to retrieve the public signing keys
 * oidc_client_id - () client ID of this i2b2 instance
 * oidc_user_field - () claim field containing the username
 *
 * 2 JWT fields are checked:
 * - audience must match the client ID
 * - username must match i2b2 username
 */
public class SecurityAuthenticationOIDC implements SecurityAuthentication {

	private static Log log = LogFactory.getLog(SecurityAuthenticationOIDC.class);

	private static JwkProvider jwkProvider = null;
	private static String jwksUri = "";
	private static JwkProvider getJwkProvider(String pJwksUri) throws MalformedURLException {
		if (!jwksUri.equals(pJwksUri) || jwkProvider == null) {
			jwksUri = pJwksUri;
			jwkProvider = new GuavaCachedJwkProvider(new UrlJwkProvider(new URL(jwksUri)));
		}
		return jwkProvider;
	}

	/**
	 * @param username username provided via the i2b2 API headers
	 * @param tokenString JWT provided via the i2b2 API headers password field
	 * @param params i2b2 parameters containing OIDC configuration
	 *
	 * @return true if authentication valid
	 * @throws Exception if authentication not valid or any kind of error
	 */
	@Override
	public boolean validateUser(String username, String tokenString, Hashtable params) throws Exception {

		try {

			String pJwksUri = (String) params.get("oidc_jwks_uri"),
				pClientId = (String) params.get("oidc_client_id"),
				pUserField = (String) params.get("oidc_user_field");

			log.debug("validateUser() with jwks URI:" + pJwksUri);

			DecodedJWT jwt = com.auth0.jwt.JWT.decode(tokenString);
			Jwk jwk = getJwkProvider(pJwksUri).get(jwt.getKeyId());
			RSAPublicKey signingPubKey = (RSAPublicKey) jwk.getPublicKey();

			// verif algorithm and signing key
			if (signingPubKey == null || !jwk.getAlgorithm().equals("RS256")) {
				throw new Exception("Rejected authentication: Problematic public key = " + signingPubKey + ", algo = " + jwk.getAlgorithm());
			}

			// verif client ID
			if (!jwt.getAudience().contains(pClientId)) {
				throw new Exception("Rejected authentication: Client ID does not match: " + pClientId + ", audience: " + jwt.getAudience().toString());
			}

			// verif JWT user matches i2b2 user
			if (!username.equals(jwt.getClaim(pUserField).asString())) {
				throw new Exception("Rejected authentication: Usernames do no match: " + username + ", " + jwt.getClaim(pUserField).asString());
			}

			// verif signature
			com.auth0.jwt.JWT
					.require(Algorithm.RSA256(signingPubKey, null))
					.build()
					.verify(tokenString);

		} catch (Exception e) {
			log.warn("Failed authentication: " + e.getMessage());
			throw new Exception("Token is invalid, please request a new one: " + e.getMessage(), e);
		}

		log.debug("validateUser() validation successful");
		return true;
	}
}
