package edu.harvard.i2b2.pm.util;

import edu.harvard.i2b2.pm.ws.ServicesMessage;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Aug 9, 2021 2:55:23 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class SecurityAuthenticationSAML implements SecurityAuthentication {

    private static final Log LOGGER = LogFactory.getLog(ServicesMessage.class);

    private static final String EPPN_HEADER = "X-eduPersonPrincipalName";
    private static final String SESSION_ID_HEADER = "X-Shib-Session-ID";

    /**
     * Verify that the login SAML
     *
     * @param username
     * @param password
     * @param params
     * @return
     * @throws Exception when authentication fails.
     */
    @Override
    public boolean validateUser(String username, String password, Hashtable params) throws Exception {
        if (username == null || password == null || params == null) {
            return false;
        }

        String eppn = params.containsKey(EPPN_HEADER) ? ((String) params.get(EPPN_HEADER)) : "";
        String sessionId = params.containsKey(SESSION_ID_HEADER) ? ((String) params.get(SESSION_ID_HEADER)) : "";
        if (!(username.equals(eppn) && password.equals(sessionId))) {
            throw new Exception("Invalid username and/or password.");
        }

        return true;
    }

}
