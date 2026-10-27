package com.trading.platform.eztrade.security.controller;

import com.trading.platform.eztrade.security.dto.JwtResponse;
import com.trading.platform.eztrade.security.dto.LoginRequest;
import com.trading.platform.eztrade.security.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for managing authentication operations.
 * <p>
 * Exposes the login endpoint, validates user credentials, and returns a JWT
 * token when authentication succeeds.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Authentication service responsible for login logic and JWT generation.
     */
    private final AuthService authService;

    /**
     * Creates a new authentication controller instance.
     *
     * @param authService authentication service used to validate credentials
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint.
     * <p>
     * Receives user credentials, delegates authentication to {@code AuthService},
     * and returns a JWT token when authentication succeeds.
     *
     * @param request object with the identifier (email or username) and user password
     * @return HTTP 200 response with the JWT token in the body
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @RequestBody @Valid LoginRequest request) {

        String token = authService.login(
                request.getIdentifier(),
                request.getPassword()
        );

        return ResponseEntity.ok(new JwtResponse(token));
    }
}

