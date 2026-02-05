package com.trading.platform.eztrade.user.domain;

/**
 * Enumeración que representa los distintos roles que puede tener un usuario
 * dentro de la plataforma.
 * <p>
 * Los roles se utilizan para gestionar la autorización y determinar
 * los permisos de acceso a los distintos recursos del sistema.
 */
public enum Role {

    /**
     * Rol con privilegios administrativos.
     * <p>
     * Suele tener acceso a operaciones de gestión avanzadas
     * y configuración del sistema.
     */
    ADMIN,

    /**
     * Rol por defecto para los usuarios finales de la aplicación.
     * <p>
     * Dispone de permisos básicos para operar en la plataforma.
     */
    USER;
}
