package com.trading.platform.eztrade.security.jwt;

import com.trading.platform.eztrade.user.api.LoadUserForSecurityPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * JWT-based authentication provider.
 * <p>
 * Encapsulates access to the user-loading port so it can be integrated with
 * Spring Security infrastructure.
 */
@Component
public class JwtAuthenticationProvider {

    /**
     * Application port responsible for loading the user data required for authentication.
     */
    private final LoadUserForSecurityPort userPort;

    /**
     * Creates a new JWT authentication provider instance.
     *
     * @param userPort port used to obtain the user's details
     */
    public JwtAuthenticationProvider(LoadUserForSecurityPort userPort) {
        this.userPort = userPort;
    }

    /**
     * Loads a user's details from its username.
     *
     * @param username user identifier (for example, email)
     * @return user details required for authentication
     */
    public UserDetails loadByUsername(String username) {
        return userPort.loadByUsername(username);
    }
}

