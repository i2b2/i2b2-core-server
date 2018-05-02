package edu.harvard.i2b2.pm.util;

import edu.harvard.i2b2.pm.delegate.ServicesHandler;
import org.junit.Test;

import java.util.Hashtable;

import static org.junit.Assert.*;

public class SecurityAuthenticationOIDCTest {

    @Test
    public void test() throws Exception {
        String classname = "edu.harvard.i2b2.pm.util.SecurityAuthenticationOIDC";
        ClassLoader classLoader = ServicesHandler.class.getClassLoader();
        Class securityClass = classLoader.loadClass(classname);
        SecurityAuthentication security =  (SecurityAuthentication) securityClass.newInstance();

        // todo: token must be fresh
        String user = "test",
                token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJlVEZyZHlyTnhYTE5ISTdwMFl3eWJjN3oxU0JIVEVjcVdjTVR5YnRkdlFZIn0.eyJqdGkiOiIxYWUzZGYwZC0yYzgyLTQzNTQtYjM1Zi04OTQ4NjRlNzI3N2YiLCJleHAiOjE1MjQ2NDg1NjQsIm5iZiI6MCwiaWF0IjoxNTI0NjQ3NjY0LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODEvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjoiaTJiMi1sb2NhbCIsInN1YiI6ImRmMTEwZjgwLWZhMzItNDE3NC05NzBkLWU0MmE3YjI0YWU5ZiIsInR5cCI6IklEIiwiYXpwIjoiaTJiMi1sb2NhbCIsIm5vbmNlIjoiTjAuMTQzNDgyMDA0MjUwNTUzMTMxNTI0NjQ3NjU5NzYyIiwiYXV0aF90aW1lIjoxNTI0NjQ2NjcwLCJzZXNzaW9uX3N0YXRlIjoiNjYxYWExMTAtYzQ4NS00MzNhLWFjMzMtMzM3ODgyZjJjMmE5IiwiYXRfaGFzaCI6IlZKNzZSN3NMbEItWDcxUi1VWUpZZWciLCJhY3IiOiIwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdCIsImVtYWlsIjoidGVzdEB0ZXN0LmNvbSJ9.c1WBLQg5kIh0RmzUehKcJjZvlacWUnEf90b4KLgGmrgiz4bVhgtGJpX3LHVO-hWJGjpMWmU-338LmMb4B3bM5WqZLNOgr9i6RWT_GMOF5yhdxKOiu4_4_clcNMF_xnSsPw4_llWGhvYLSSUByH4doe8MfMlu6jIbzYUsbv80bUHK5eWb6fxfr_sp9R0k_OQlMB5OHtPUhncBzQ5MzXvc8hFGvvjTNx-x-zgJcVxZNFzGjkh7Al53zyLOtS62McSIWxNyRW5okDQ3CU6QXLPC7nAa9Op4RLPU4W35Ps2x9nIMV8vcYiPt1u8lOXzIcO7bscv8YRJwYeMEYwfrOIGedQ";

        Hashtable params = new Hashtable();
        params.put("oidc_jwks_uri", "http://localhost:8081/auth/realms/master/protocol/openid-connect/certs");
        params.put("oidc_client_id", "i2b2-local");
        params.put("oidc_user_field", "preferred_username");

        assertTrue(security.validateUser(user, token, params));

        params.replace("oidc_client_id", "wrong");
        try {
            security.validateUser(user, token, params);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertTrue(true);
        }

        params.replace("oidc_client_id", "i2b2-local");
        params.replace("oidc_user_field", "email");
        try {
            security.validateUser(user, token, params);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertTrue(true);
        }
    }
}
