/**
 * <h2>UserExistsException</h2>
 *
 * <p><strong>Módulo:</strong> Domain</p>
 * <p><strong>Capa:</strong> Domain</p>
 *
 * <p><strong>Responsabilidad:</strong><br/>
 * Encapsula lógica de negocio pura del dominio.</p>
 *
 * <p><strong>Rol arquitectónico:</strong><br/>
 * Forma parte de la arquitectura hexagonal del módulo, manteniendo separación
 * entre dominio, aplicación e infraestructura mediante puertos y adaptadores.
 * Está gestionado por Spring Modulith.</p>
 */

package com.trading.platform.eztrade.user.domain.exceptions;

public class UserExistsException extends RuntimeException {
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @param message dato de entrada requerido por la operación.
     * @return resultado devuelto por la operación.
     */
    public UserExistsException(String message) {
        super(message);
    }
}
