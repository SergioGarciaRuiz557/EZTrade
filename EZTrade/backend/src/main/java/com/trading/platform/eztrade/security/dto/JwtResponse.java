package com.trading.platform.eztrade.security.dto;

/**
 * Respuesta que encapsula la información del token JWT generado tras la autenticación.
 * <p>
 * Incluye el propio token y el tipo de esquema de autenticación utilizado
 * en la cabecera <strong>Authorization</strong>.
 */
public class JwtResponse {

    /**
     * Token JWT firmado que el cliente utilizará para autenticarse
     * en las siguientes peticiones.
     */
    private final String token;

    /**
     * Tipo de token utilizado en la cabecera <strong>Authorization</strong>.
     * Por defecto, se usa el esquema <strong>Bearer</strong>.
     */
    private final String type = "Bearer";

    /**
     * Crea una nueva respuesta JWT con el token proporcionado.
     *
     * @param token token JWT generado por el sistema de autenticación
     */
    public JwtResponse(String token) {
        this.token = token;
    }

    /**
     * Devuelve el token JWT.
     *
     * @return token JWT firmado
     */
    public String getToken() {
        return token;
    }

    /**
     * Devuelve el tipo de token utilizado.
     *
     * @return tipo de token, normalmente <strong>Bearer</strong>
     */
    public String getType() {
        return type;
    }
}


