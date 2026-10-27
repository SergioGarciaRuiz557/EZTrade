package com.trading.platform.eztrade.user.api;

import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Input port for loading users in the security context.
 * <p>
 * This port is used by the infrastructure layer (for example, Spring Security
 * adapters) to obtain a user's details from its identifier.
 */
@NamedInterface
public interface LoadUserForSecurityPort {

    /**
     * Loads a user's details from its username.
     *
     * @param username user identifier, usually the email address
     * @return {@link UserDetails} instance with the user's security information
     */
    UserDetails loadByUsername(String username);
}

