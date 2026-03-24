package com.trading.platform.eztrade.security.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa la petición de inicio de sesión del usuario.
 * <p>
 * Contiene las credenciales necesarias para autenticarse en el sistema:
 * email o username, y contraseña.
 */
public class LoginRequest {

    /**
     * Correo electrónico del usuario (opcional si se informa username).
     */
    private String email;

    /**
     * Nombre de usuario (opcional si se informa email).
     */
    private String username;

    /**
     * Contraseña del usuario.
     * <p>
     * Campo obligatorio.
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
     * Devuelve el correo electrónico del usuario.
     *
     * @return email del usuario
     */
    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Devuelve el identificador de login normalizado (email o username).
     *
     * @return email si está informado; en caso contrario username
     */
    public String getIdentifier() {
        return hasText(email) ? email : username;
    }

    /**
     * Devuelve la contraseña del usuario.
     *
     * @return contraseña del usuario
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


