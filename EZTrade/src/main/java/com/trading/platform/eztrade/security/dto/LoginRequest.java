package com.trading.platform.eztrade.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    // getters
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
}

