package com.trading.platform.eztrade.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa la petición de inicio de sesión del usuario.
 * <p>
 * Contiene las credenciales necesarias para autenticarse en el sistema:
 * correo electrónico y contraseña.
 */
public class LoginRequest {

    /**
     * Correo electrónico del usuario.
     * <p>
     * Debe tener un formato válido y no puede estar vacío.
     */
    @Email
    @NotBlank
    private String email;

    /**
     * Contraseña del usuario.
     * <p>
     * Campo obligatorio.
     */
    @NotBlank
    private String password;

    /**
     * Devuelve el correo electrónico del usuario.
     *
     * @return email del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Devuelve la contraseña del usuario.
     *
     * @return contraseña del usuario
     */
    public String getPassword() {
        return password;
    }
}


