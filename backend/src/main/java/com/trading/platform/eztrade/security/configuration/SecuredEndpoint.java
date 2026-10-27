package com.trading.platform.eztrade.security.configuration;

import org.springframework.http.HttpMethod;

/**
 * Contract representing a secured application endpoint.
 * <p>
 * Each implementation defines the path, HTTP method, and security policy
 * associated with the exposed resource.
 */
public interface SecuredEndpoint {

    /**
     * Returns the protected endpoint path.
     *
     * @return endpoint path (for example, <strong>`/api/user`</strong>)
     */
    String path();

    /**
     * Returns the HTTP method associated with the endpoint.
     *
     * @return HTTP method required to access the resource
     */
    HttpMethod method();

    /**
     * Returns the security policy applied to the endpoint.
     *
     * @return security policy that must be satisfied to access it
     */
    SecurityPolicy policy();
}

