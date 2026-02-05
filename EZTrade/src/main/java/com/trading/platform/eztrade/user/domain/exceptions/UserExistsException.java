package com.trading.platform.eztrade.user.domain.exceptions;

/**
 * Excepción de dominio que indica que el usuario que se intenta registrar
 * ya existe en el sistema.
 * <p>
 * Se lanza desde la capa de aplicación cuando se detecta un conflicto
 * al crear un nuevo usuario (por ejemplo, email duplicado).
 */
public class UserExistsException extends RuntimeException {

    /**
     * Crea una nueva instancia de la excepción con el mensaje detallado.
     *
     * @param message descripción del motivo por el que el usuario ya existe
     */
    public UserExistsException(String message) {
        super(message);
    }
}
