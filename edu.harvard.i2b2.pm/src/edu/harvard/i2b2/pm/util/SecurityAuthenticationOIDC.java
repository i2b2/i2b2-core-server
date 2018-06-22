package edu.harvard.i2b2.pm.util;

import com.auth0.jwk.*;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Hashtable;

/*
 * OpenID Connect authentication for i2b2 v1.6
 * Supports JWT verification signed with RS256 and keys retrieved via JWKS.
 * Token is passed through the password field of the XML.
 *
 * The parameters are listed below with their possible values in ():
 * authentication_method - (OIDC)
 * oidc_jwks_uri - () URI of JWKS to retrieve the public signing keys
 * oidc_client_id - () client ID of this i2b2 instance
 * oidc_user_field - () claim field containing the username
 * oidc_token_issuer - () token issuer
 *
 * 2 JWT fields are checked: https://openid.net/specs/openid-connect-core-1_0.html#ImplicitIDTValidation
 * - audience must match the client ID
 * - username must match i2b2 username
 * - nonce check is made by the caller
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
				pUserField = (String) params.get("oidc_user_field"),
				pTokenIssuer = (String) params.get("oidc_token_issuer");

			log.debug("validateUser() with jwks URI:" + pJwksUri);

			DecodedJWT jwt = com.auth0.jwt.JWT.decode(tokenString);
			Jwk jwk = getJwkProvider(pJwksUri).get(jwt.getKeyId());
			RSAPublicKey signingPubKey = (RSAPublicKey) jwk.getPublicKey();

			// verif algorithm and signing key
			if (signingPubKey == null || !jwk.getAlgorithm().equals("RS256")) {
				throw new Exception("Rejected authentication: Problematic public key = " + signingPubKey + " or algo = " + jwk.getAlgorithm());
			}

			// token validation
			com.auth0.jwt.JWT
					.require(Algorithm.RSA256(signingPubKey, null))
					.withIssuer(pTokenIssuer) // check issuer
					.withAudience(pClientId) // check audience matches
					.withClaim(pUserField, username) // check username matches i2b2 internal username
					.build()
					.verify(tokenString); // check time validity and signature

		} catch (Exception e) {
			log.warn("Failed authentication: " + e.getMessage());
			throw new Exception("Token is invalid, please request a new one: " + e.getMessage(), e);
		}

		log.debug("validateUser() validation successful");
		return true;
	}
}
