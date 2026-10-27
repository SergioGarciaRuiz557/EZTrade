package com.trading.platform.eztrade.security.configuration;

/**
 * Semantic access policies that the security configuration can use.
 * <p>
 * Express whether an endpoint is public, requires authentication, or is reserved
 * for administrators.
 */
public enum SecurityPolicy {
    /** Endpoint accessible without a JWT. */
    PUBLIC,
    /** Endpoint that requires an authenticated user. */
    AUTHENTICATED,
    /** Endpoint reserved for users with an administrator role. */
    ADMIN
}
