package com.trading.platform.eztrade.security.configuration;

/**
 * Politicas semanticas de acceso que puede usar la configuracion de seguridad.
 * <p>
 * Sirven para expresar si un endpoint es publico, requiere autenticacion o
 * queda reservado a administradores.
 */
public enum SecurityPolicy {
    /** Endpoint accesible sin JWT. */
    PUBLIC,
    /** Endpoint que requiere usuario autenticado. */
    AUTHENTICATED,
    /** Endpoint reservado a usuarios con rol de administracion. */
    ADMIN
}
