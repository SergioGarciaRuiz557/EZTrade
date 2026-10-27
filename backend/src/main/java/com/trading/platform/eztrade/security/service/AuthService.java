package com.trading.platform.eztrade.security.service;

import com.trading.platform.eztrade.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * User authentication service.
 * <p>
 * Validates the received credentials and generates a JWT token for the
 * authenticated user.
 */
@Service
public class AuthService {

    /**
     * Spring Security component responsible for performing the authentication
     * process with the provided credentials.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Service responsible for generating JWT tokens for authenticated users.
     */
    private final JwtService jwtService;

    /**
     * Creates a new authentication service instance.
     *
     * @param authenticationManager Spring Security authentication manager
     * @param jwtService            service used to generate JWT tokens
     */
    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates a user with email or username and password, then generates a JWT token.
     *
     * @param identifier user email or username
     * @param password user plaintext password
     * @return JWT token associated with the authenticated user
     * @throws org.springframework.security.core.AuthenticationException
     *         if the credentials are invalid
     */
    public String login(String identifier, String password) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(identifier, password)
                );

        return jwtService.generateToken(
                (UserDetails) authentication.getPrincipal()
        );
    }
}


