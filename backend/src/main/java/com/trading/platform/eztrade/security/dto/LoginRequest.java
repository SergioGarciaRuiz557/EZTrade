package com.trading.platform.eztrade.security.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO representing the user's login request.
 * <p>
 * Contains the credentials required to authenticate in the system: email or
 * username, plus password.
 */
public class LoginRequest {

    /**
     * User email address (optional if username is provided).
     */
    private String email;

    /**
     * Username (optional if email is provided).
     */
    private String username;

    /**
     * User password.
     * <p>
     * Required field.
     */
    @NotBlank
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the user email address.
     *
     * @return user email
     */
    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Returns the normalized login identifier (email or username).
     *
     * @return email if present; otherwise username
     */
    public String getIdentifier() {
        return hasText(email) ? email : username;
    }

    /**
     * Returns the user password.
     *
     * @return user password
     */
    public String getPassword() {
        return password;
    }

    @AssertTrue(message = "Either email or username must be provided")
    public boolean isIdentifierPresent() {
        return hasText(email) || hasText(username);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}


